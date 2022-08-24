
/*
 * Copyright 2010 - 2020 Anywhere Software (www.b4x.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 package anywheresoftware.b4a.objects;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponseCode;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetails.OneTimePurchaseOfferDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsParams.Product;
import com.android.billingclient.api.QueryPurchasesParams;

import android.app.Activity;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.Permissions;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;

/**
 * Wrapper for <link>Google Play Billing Library|https://developer.android.com/google/play/billing/billing_library_overview</link>.
 * Replaces the InAppBilling3 library which is now deprecated.
 *The PurchasesUpdated event will be raised in the module where BillingClient was initialized. The other events should be handled with Wait For.
 * 
 */
@Events(values = { "Connected (Result As BillingResult)",
		"SkuQueryCompleted (Result As BillingResult, SkuDetails As List)",
		"PurchasesQueryCompleted (Result As BillingResult, Purchases As List)",
		"PurchasesUpdated (Result As BillingResult, Purchases As List)",
		"ConsumeCompleted (Result As BillingResult)",
"AcknowledgeCompleted (Result As BillingResult)"})
@Permissions(values= {"com.android.vending.BILLING"})
@Version(5.0f)
@DependsOn(values = { "billing-5.0.0.aar" })
@ShortName("BillingClient")
public class BillingClientWrapper {
	private String eventName;
	@Hide
	public BillingClient client;

	/**
	 * Initializes the billing client.
	 */
	public void Initialize(BA ba, String EventName) {
		InitializeWithBuilder(ba, EventName, BillingClient.newBuilder(ba.context).enablePendingPurchases());
	}
	/**
	 * Alternative initialization method that accepts a custom client builder.
	 */
	public void InitializeWithBuilder(final BA ba, String EventName, BillingClient.Builder Builder) {
		this.eventName = EventName.toLowerCase(BA.cul);
		Builder.setListener(new PurchasesUpdatedListener() {

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void onPurchasesUpdated(BillingResult arg0, List<Purchase> arg1) {
				anywheresoftware.b4a.objects.collections.List purchases = new anywheresoftware.b4a.objects.collections.List();
				if (arg1 != null)
					purchases.setObject((List)arg1);
				ba.raiseEventFromDifferentThread(BillingClientWrapper.this, null, 0, eventName + "_purchasesupdated", true, new Object[] {AbsObjectWrapper.ConvertToWrapper(new BillingResultWrapper(), arg0), purchases});

			}
		});
		client = Builder.build();
	}

	/**
	 * Connects to the store service if not already connected. Should always be called before making other requests.
	 *The Connected event will be raised.
	 *
	 *Example from the Starter service:
	 *<code>Wait For (Billing.ConnectIfNeeded) Billing_Connected (Result As BillingResult)</code>
	 *Example from other modules:
	 *<code>Wait For (Starter.Billing.ConnectIfNeeded) Billing_Connected (Result As BillingResult)
	 *If Result.IsSuccess Then
	 * 
	 *End If
	 *</code>
	 */
	public Object ConnectIfNeeded(final BA ba) {
		final Object sender = new Object();
		if (client.isReady() == false) {
			client.startConnection(new BillingClientStateListener() {

				@Override
				public void onBillingSetupFinished(BillingResult arg0) {
					ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_connected", false,
							new Object[] { AbsObjectWrapper.ConvertToWrapper(new BillingResultWrapper(), arg0) });
				}

				@Override
				public void onBillingServiceDisconnected() {
					ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_connected", false,
							new Object[] { createResult(false) });
				}
			});
		} else {
			ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_connected", false,
					new Object[] { createResult(true) });
		}
		return sender;
	}

	private BillingResultWrapper createResult(boolean success) {
		return (BillingResultWrapper) AbsObjectWrapper.ConvertToWrapper(new BillingResultWrapper(), BillingResult
				.newBuilder().setResponseCode(success ? BillingResponseCode.OK : BillingResponseCode.ERROR).build());
	}

	/**
	 *Gets the details of the specified SKUs.
	 *SkyType - "inapp" for regular products or "subs" for subscriptions.
	 *SKUs - List or array with the requested SKU ids.
	 *
	 *Example:<code>
	 *Dim sf As Object = Starter.Billing.QuerySkuDetails("inapp", Array("android.test.purchased"))
	 *Wait For (sf) Billing_SkuQueryCompleted (Result As BillingResult, SkuDetails As List)
	 *If Result.IsSuccess And SkuDetails.Size = 1 Then</code>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object QuerySkuDetails(BA ba, String SkuType, anywheresoftware.b4a.objects.collections.List SKUs) {
		QueryProductDetailsParams.Builder params = QueryProductDetailsParams.newBuilder();
		final Object sender = new Object();
		ArrayList<Product> products = new ArrayList<QueryProductDetailsParams.Product>();
		for (Object sku : SKUs.getObject()) {
			products.add(QueryProductDetailsParams.Product.newBuilder().setProductId((String)sku).setProductType(SkuType).build());
		}
		client.queryProductDetailsAsync(params.setProductList(products).build(),
				new ProductDetailsResponseListener() {
					
					@Override
					public void onProductDetailsResponse(BillingResult var1, List<ProductDetails> var2) {
						anywheresoftware.b4a.objects.collections.List res = new anywheresoftware.b4a.objects.collections.List();
						if (var2 != null)
							res.setObject((List)var2);
						ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_skuquerycompleted", true,
								new Object[] { AbsObjectWrapper.ConvertToWrapper(new BillingResultWrapper(), var1),
										res });
					}
				});

		return sender;
	}
	/**
	 * Gets purchases details for all owned products. Check the purchases state as pending purchases will also be listed.
	 * SkuType - "inapp" for regular purchases and "subs" for subscriptions.
	 *Example:
	 *<code>
	 *Wait For (Starter.Billing.ConnectIfNeeded) Billing_Connected (Result As BillingResult)
	 *If Result.IsSuccess Then
	 *	Wait For (Starter.Billing.QueryPurchases("inapp")) Billing_PurchasesQueryCompleted (Result As BillingResult, Purchases As List)
	 *	If Result.IsSuccess Then
	 *		For Each Purchase As Purchase In Purchases
	 *			
	 *		Next
	 *	End If
	 *End If</code>
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object QueryPurchases(BA ba, String SkuType) {
		final Object sender = new Object();
		client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(SkuType).build(), new PurchasesResponseListener() {
			
			@Override
			public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> ppurchases) {
				anywheresoftware.b4a.objects.collections.List purchases = new anywheresoftware.b4a.objects.collections.List();
				if (ppurchases != null)
					purchases.setObject((List)ppurchases);
				ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_purchasesquerycompleted", true, new Object[] {AbsObjectWrapper.ConvertToWrapper(new BillingResultWrapper(), billingResult), purchases});
			}
		});
		
		return sender;
	}
	/**
	 * Consumes a product. It will not be "owned" after this call.
	 * 
	 */
	public Object Consume(final BA ba, String PurchaseToken, String Unused) {
		final Object sender = new Object();
		client.consumeAsync(ConsumeParams.newBuilder().setPurchaseToken(PurchaseToken).build(),
				new ConsumeResponseListener() {

			@Override
			public void onConsumeResponse(BillingResult paramBillingResult, String paramString) {
				ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_consumecompleted", true, new Object[] {AbsObjectWrapper.ConvertToWrapper(new BillingResultWrapper(), paramBillingResult)});
			}
		});
		return sender;
	}
	/**
	 * All purchases must be acknowledged or consumed in 3 days. Call this method to acknowledge the purchase.
	 *PurchaseToken - The token as retrieved with Purchase.PurchaseToken.
	 *Unused - (developer payload value is no longer supported).
	 *
	 *<code>Wait For (Billing.AcknowledgePurchase(p.PurchaseToken, "")) Billing_AcknowledgeCompleted (Result As BillingResult)</code>
	 */
	public Object AcknowledgePurchase(final BA ba, String PurchaseToken, String Unused) {
		final Object sender = new Object();
		client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(PurchaseToken).build(), 
				new AcknowledgePurchaseResponseListener() {

			@Override
			public void onAcknowledgePurchaseResponse(BillingResult paramBillingResult) {
				ba.raiseEventFromDifferentThread(sender, null, 0, eventName + "_acknowledgecompleted", true, new Object[] {AbsObjectWrapper.ConvertToWrapper(new BillingResultWrapper(), paramBillingResult)});

			}
		});
		return sender;
	}
	/**
	 * Tests whether subscriptions are supported.
	 */
	public boolean getSubscriptionsSupported() {
		return client.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS)
				.getResponseCode() == BillingResponseCode.OK;
	}
	/**
	 * Starts the billing flow. Returns a BillingResult. The PurchasesUpdated event will be raised if a purchase was done (successfully or unsuccessfully).
	 *This method must be called from an Activity or a class with an activity context.
	 *<code>
	 *Result = Starter.Billing.LaunchBillingFlow(SkuDetails.Get(0))
	 *Log("LaunchBillingFlow: " & Result.IsSuccess)</code>
	 */
	public BillingResultWrapper LaunchBillingFlow (BA ba, SkuDetailsWrapper Sku) {
		Activity activity = ba.sharedProcessBA.activityBA.get().activity;
		ArrayList<ProductDetailsParams> productsDetails = new ArrayList<BillingFlowParams.ProductDetailsParams>();
		productsDetails.add(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(Sku.getObject()).build());
		return (BillingResultWrapper) AbsObjectWrapper.ConvertToWrapper(new BillingResultWrapper(),
				client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(productsDetails).build()));
	}
	/**
	 * Tests whether the purchase was signed properly.
	 *Base64Key - Key from Google Play Console.
	 */
	public boolean VerifyPurchase(PurchaseWrapper Purchase, String Base64Key) throws IOException {
		return Checker.verifyPurchase(Base64Key, Purchase.getOriginalJson(), Purchase.getSignature());
	}


	/**
	 * Product details.
	 */
	@ShortName("SkuDetails")
	public static class SkuDetailsWrapper extends AbsObjectWrapper<ProductDetails> {
		/**
		 * Returns the product id.
		 */
		public String getSku() {
			return getObject().getProductId();
		}
		/**
		 * Returns the formatted price with the currency sign for one time purchase offers.
		 */
		public String getPrice() {
			OneTimePurchaseOfferDetails details = getObject().getOneTimePurchaseOfferDetails();
			return details == null ? "" : details.getFormattedPrice();
		}
		/**
		 * Returns the product description.
		 */
		public String getDescription() {
			return getObject().getDescription();
		}
		/**
		 * Returns the product title.
		 */
		public String getTitle() {
			return getObject().getTitle();
		}

	}

	/**
	 * Information about the operation result.
	 */
	@ShortName("BillingResult")
	public static class BillingResultWrapper extends AbsObjectWrapper<BillingResult> {
		public static final int CODE_SERVICE_TIMEOUT = -3;
		public static final int CODE_FEATURE_NOT_SUPPORTED = -2;
		public static final int CODE_SERVICE_DISCONNECTED = -1;
		public static final int CODE_OK = 0;
		public static final int CODE_USER_CANCELED = 1;
		public static final int CODE_SERVICE_UNAVAILABLE = 2;
		public static final int CODE_BILLING_UNAVAILABLE = 3;
		public static final int CODE_ITEM_UNAVAILABLE = 4;
		public static final int CODE_DEVELOPER_ERROR = 5;
		public static final int CODE_ERROR = 6;
		public static final int CODE_ITEM_ALREADY_OWNED = 7;
		public static final int CODE_ITEM_NOT_OWNED = 8;
		/**
		 * Tests whether the result was successful. Logs the response code and debug string in debug mode when the result was unsuccessful.
		 */
		public boolean getIsSuccess() {
			boolean b = getObject().getResponseCode() == BillingResponseCode.OK;
			if (!b && BA.debugMode) {
				BA.LogInfo("BillingResult IsSuccess = False, ResponseCode = " + getResponseCodeString());
				if (getDebugMessage().length() > 0)
					BA.LogInfo("Debug string: " + getDebugMessage());
			}
			return b;
		}
		/**
		 * Returns the response code. One of the CODE constants.
		 */
		public int getResponseCode() {
			return getObject().getResponseCode();
		}
		/**
		 * Returns the response code as a string.
		 */
		public String getResponseCodeString()  {
			int i = getResponseCode();
			try {

				for (Field f : this.getClass().getDeclaredFields()) {
					if (f.getName().startsWith("CODE_") && ((int)f.get(null)) == i)
						return f.getName().substring("CODE_".length());
				}
			} catch (Exception e) {
				e.printStackTrace();
				;
			}
			return "UNKNOWN (" + i + ")";
		}

		/**
		 * Returns the debug message.
		 */
		public String getDebugMessage() {
			return BA.returnString(getObject().getDebugMessage());
		}

	}

	@ShortName("Purchase")
	public static class PurchaseWrapper extends AbsObjectWrapper<Purchase> {
		public static final int STATE_UNSPECIFIED = 0;
		public static final int STATE_PURCHASED = 1;
		public static final int STATE_PENDING = 2;
		/**
		 * Returns an unique order identifier for the transaction.
		 */
		public String getOrderId() {
			return getObject().getOrderId();
		}
		/**
		 * Returns the product id 
		 */
		public String getSku() {
			return getObject().getProducts().size() == 0 ? "" : getObject().getProducts().get(0);
		}
		/**
		 * Returns the time the product was purchased.
		 */
		public long getPurchaseTime() {
			return getObject().getPurchaseTime();
		}
		/**
		 * Returns a token that uniquely identifies a purchase for a given time and user pair.
		 */
		public String getPurchaseToken() {
			return getObject().getPurchaseToken();
		}
		/**
		 * Returns the purchase state. One of the STATE constants.
		 */
		public int getPurchaseState() {
			return getObject().getPurchaseState();
		}
		/**
		 * Deprecated.
		 */
		public String getDeveloperPayload() {
			return BA.returnString(getObject().getDeveloperPayload());
		}
		/**
		 * Tests whether the purchase was acknowledged. All purchases should be acknowledged or consumed in 3 days.
		 */
		public boolean getIsAcknowledged() {
			return getObject().isAcknowledged();
		}
		/**
		 * Tests whether the subscription renews automatically.
		 */
		public boolean getIsAutoRenewing() {
			return getObject().isAutoRenewing();
		}
		/**
		 *Gets the json string that includes the purchase information. 
		 */
		public String getOriginalJson() {
			return getObject().getOriginalJson();
		}
		/**
		 * Gets the purchase data signature.
		 */
		public String getSignature() {
			return getObject().getSignature();
		}

	}

}
