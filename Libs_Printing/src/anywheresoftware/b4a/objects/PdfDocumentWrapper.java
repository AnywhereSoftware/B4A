
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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.v4.print.PrintHelper;
import android.support.v4.print.PrintHelper.OnPrintFinishCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.ActivityObject;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Events;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.drawable.CanvasWrapper;
import anywheresoftware.b4a.objects.streams.File;
import anywheresoftware.b4a.objects.streams.File.InputStreamWrapper;

/**
 * PdfDocument can be used to create Pdf files with one or more pages.
 *Example:<code>
 *pdf.Initialize
 *pdf.StartPage(595, 842) 'A4 size
 *pdf.Canvas.DrawLine(2, 2, 593 , 840, Colors.Blue, 4)
 *pdf.Canvas.DrawText("Hello", 100, 100, Typeface.DEFAULT_BOLD, 30, Colors.Yellow, "CENTER")
 *pdf.FinishPage
 *Dim out As OutputStream = File.OpenOutput(File.DirRootExternal, "1.pdf", False)
 *pdf.WriteToStream(out)
 *out.Close
 *pdf.Close</code>
 */
@DependsOn(values={"com.android.support:support-v4"})
@Version(1.11f)
@ShortName("PdfDocument")
public class PdfDocumentWrapper {
	@Hide
	public PdfDocument document;
	@Hide
	public Page page;
	private CanvasWrapper cw;
	public void Initialize() {
		document = new PdfDocument();
	}
	/**
	 * Starts a new page. Make sure to call FinishPage when you are done drawing.
	 *Width / Height - Page dimension measured in Postscript (1/72th of an inch).
	 */
	public void StartPage(int Width, int Height) {
		page = document.startPage(new PageInfo.Builder(Width, Height, document.getPages().size() + 1).create());
		cw = new CanvasWrapper();
		cw.Initialize2(Bitmap.createBitmap(1, 1, Config.ARGB_8888));
		cw.canvas = page.getCanvas();
	}
	/**
	 * Returns the canvas that is used to draw on the current page.
	 *Note that you <b>should not</b> use DIP units with this canvas.
	 *Canvas.Bitmap will return a stub bitmap.
	 */
	public CanvasWrapper getCanvas() {
		return cw;
	}
	/**
	 * Finalizes the page drawings.
	 */
	public void FinishPage() {
		document.finishPage(page);
	}
	/**
	 * Writes the document to the output stream.
	 */
	public void WriteToStream(OutputStream out) throws IOException {
		document.writeTo(out);
	}
	/**
	 * Closes the document.
	 */
	public void Close() {
		document.close();
	}
	/**
	 * The printer object allows printing bitmaps, html strings and WebView content using the system printing feature.
	 */
	@ActivityObject
	@ShortName("Printer")
	public static class Printer {
		private BA ba;
		@SuppressWarnings("unused")
		private String eventName;
		@Hide
		public PrintHelper ph;
		@Hide
		public WebView wv;
		private String globalJobName;
		/**
		 * Initializes the printer object. Currently there are no events.
		 */
		public void Initialize(BA ba, String EventName) {
			this.ba = ba;
			this.eventName = EventName.toLowerCase(BA.cul);
			if (PrintHelper.systemSupportsPrint())
				ph = new PrintHelper(ba.context);
		}
		public boolean getPrintSupported() {
			return PrintHelper.systemSupportsPrint();
		}
		/**
		 * Prints a bitmap. The system printing dialog will appear.
		 *JobName - The print job name. 
		 *Bitmap - Bitmap to print.
		 *Fit - If true then the bitmap will be scaled to fit, otherwise it will be scaled to fill and will be cropped.
		 */
		public void PrintBitmap(String JobName, Bitmap Bitmap, boolean Fit) {
			ph.setScaleMode(Fit ? PrintHelper.SCALE_MODE_FIT : PrintHelper.SCALE_MODE_FILL);
			ph.printBitmap(JobName, Bitmap, null);
		}
		/**
		 * Prints the provided html string.
		 */
		public void PrintHtml(String JobName, String Html) {
			globalJobName = JobName;
			if (wv == null) {
				wv = new WebView(ba.context);
				wv.setWebViewClient(new WebViewClient() {
					@Override
		            public void onPageFinished(WebView view, String url) {
						
		            	PrintWebView(globalJobName, wv);
		            }
				});
			}
			wv.loadDataWithBaseURL("file:///", Html, "text/html", "UTF8", null);
		}
		/**
		 * Prints the WebView content. Make sure to wait for the PageFinished event.
		 */
		public void PrintWebView(String JobName, WebView WebView) {
			BA.Log("JobName: " + JobName);
			PrintWebView(JobName, WebView, new PrintAttributes.Builder().build());
		}
		@SuppressWarnings("deprecation")
		@Hide
		public void PrintWebView(String JobName, WebView WebView, PrintAttributes pa) {
			PrintManager printManager = (PrintManager) ba.context
			.getSystemService(Context.PRINT_SERVICE);

			PrintDocumentAdapter printAdapter = WebView.createPrintDocumentAdapter();
			printManager.print(JobName, printAdapter, pa);
		}
		/**
		 * Prints a pdf document.
		 *JobName - The print job name.
		 *Dir / FileName - PDF file.
		 */
		public void PrintPdf (final String JobName, final String Dir, final String FileName) {
			PrintPdf2(JobName, Dir, FileName, null);
		}
		/**
		 * Similar to PrintPdf. Allows passing custom PrintAttributes.
		 */
		public void PrintPdf2(final String JobName, final String Dir, final String FileName, PrintAttributes Attributes) {
			PrintManager printManager = (PrintManager) ba.activity.getSystemService(Context.PRINT_SERVICE);
			PrintDocumentAdapter pda = new PrintDocumentAdapter() {
				@Override
				public void onLayout(PrintAttributes oldAttributes,
						PrintAttributes newAttributes,
						CancellationSignal cancellationSignal,
						LayoutResultCallback callback, Bundle extras) {
					 if (cancellationSignal.isCanceled()) {
				            callback.onLayoutCancelled();
				            return;
				        }
				        PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(JobName).setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build();

				        callback.onLayoutFinished(pdi, true);
					
				}

				@Override
				public void onWrite(PageRange[] pages,
						ParcelFileDescriptor destination,
						CancellationSignal cancellationSignal,
						WriteResultCallback callback) {
					FileOutputStream out = new FileOutputStream(destination.getFileDescriptor());
					try {
						File.Copy2(File.OpenInput(Dir, FileName).getObject(), out);
						out.close();
					
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
					
				}
				
			};
			printManager.print(JobName, pda, Attributes);
		}
		
	}
}
