
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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.DateTime;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableHyperlink;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.WritableFont.FontName;
import jxl.write.biff.RowsExceededException;
import anywheresoftware.b4a.AbsObjectWrapper;
import anywheresoftware.b4a.BA.DependsOn;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.objects.streams.File;

/**
 * This library wraps the open source jexcel project. It allows reading and writing Microsoft Excel files.
 *See the tutorial in the forum for more information.
 */
@DependsOn(values={"jxl"})
@ShortName("ReadableWorkbook")
@Version(1.00f)
public class WorkbookWrapper extends AbsObjectWrapper<Workbook>{
	/**
	 * Gets or sets the encoding. Must be set before Initialize is called.
	 */
	public static String Encoding = "Cp1252";
	/**
	 * Gets the sheet at the specified index (first sheet is at index 0).
	 */
	public SheetWrapper GetSheet(int Index) {
		SheetWrapper sh = new SheetWrapper();
		sh.setObject(getObject().getSheet(Index));
		return sh;
	}
	/**
	 * Opens the given workbook in read-only mode.
	 */
	public void Initialize(String Dir, String FileName) throws IOException, BiffException {
		InputStream in = File.OpenInput(Dir, FileName).getObject();
		Workbook w = Workbook.getWorkbook(in, getDefaultSettings(Encoding));
		in.close();
		setObject(w);
	}
	/**
	 * Returns the number of sheets.
	 */
	public int getNumberOfSheets() {
		return getObject().getNumberOfSheets();
	}
	/**
	 * Returns an array with the sheets names.
	 */
	public String[] GetSheetNames() {
		return getObject().getSheetNames();
	}
	/**
	 * Closes the workbook.
	 */
	public void Close() {
		if (IsInitialized())
			getObject().close();
	}
	static WorkbookSettings getDefaultSettings(String encoding) {
		WorkbookSettings ws = new WorkbookSettings();
		ws.setUseTemporaryFileDuringWrite(true);
		ws.setEncoding(encoding);
		ws.setTemporaryFileDuringWriteDirectory(new java.io.File(File.getDirInternalCache()));
		return ws;
	}
	@ShortName("WritableWorkbook")
	public static class WritableWorkbookWrapper extends AbsObjectWrapper<WritableWorkbook> {
		/**
		 * Gets or sets the encoding. Must be set before Initialize is called.
		 */
		public static String Encoding = "Cp1252";
		/**
		 * Initializes the output file. <b>The output file will be deleted if it already exists.</b>
		 */
		public void Initialize(String Dir, String FileName) throws IOException {
			OutputStream out = File.OpenOutput(Dir, FileName, false).getObject();
			WritableWorkbook ww = Workbook.createWorkbook(out, getDefaultSettings(Encoding));
			setObject(ww);
		}
		/**
		 * Initializes the output file and creates a copy of the given workbook.
		 *<b>The output file will be deleted if it already exists.</b>
		 */
		public void Initialize2(String Dir, String FileName, Workbook Workbook) throws IOException {
			OutputStream out = File.OpenOutput(Dir, FileName, false).getObject();
			WritableWorkbook ww = Workbook.createWorkbook(out, Workbook, getDefaultSettings(Encoding));
			setObject(ww);
		}
		/**
		 * Returns the number of sheets.
		 */
		public int getNumberOfSheets() {
			return getObject().getNumberOfSheets();
		}
		/**
		 * Returns an array with the sheets names.
		 */
		public String[] GetSheetNames() {
			return getObject().getSheetNames();
		}
		/**
		 * Gets the sheet at the specified index (first sheet is at index 0).
		 */
		public WritableSheetWrapper GetSheet(int Index) {
			return (WritableSheetWrapper) 
			AbsObjectWrapper.ConvertToWrapper(new WritableSheetWrapper(), getObject().getSheet(Index));
		}
		/**
		 * Adds a new sheet with the given name and at the specific index.
		 */
		public WritableSheetWrapper AddSheet(String Name, int Index) {
			return (WritableSheetWrapper) 
				AbsObjectWrapper.ConvertToWrapper(new WritableSheetWrapper(), getObject().createSheet(Name, Index));
		}
		/**
		 * Removes the sheet at the given index.
		 */
		public void RemoveSheet(int Index) {
			getObject().removeSheet(Index);
		}
		/**
		 * Writes the data to the disk.
		 */
		public void Write() throws IOException {
			getObject().write();
		}
		/**
		 * Closes the workbook.
		 */
		public void Close() throws WriteException, IOException {
			if (IsInitialized())
				getObject().close();
		}
	}

	@Hide
	public static class AbsSheetWrapper<T extends Sheet> extends AbsObjectWrapper<T> {
		/**
		 * Gets the sheet's name.
		 */
		public String getName() {
			return getObject().getName();
		}
		/**
		 * Returns the string value of the cell at the given column and row.
		 *(0, 0) is equivalent to A0.
		 *(0, 1) is equivalent to A1.
		 *(1, 0) is equivalent to B0.
		 */
		public String GetCellValue(int Col, int Row) {
			return getObject().getCell(Col, Row).getContents();
		}
		/**
		 * Returns the number of rows.
		 */
		public int getRowsCount() {
			return getObject().getRows();
		}
		/**
		 * Returns the number of columns.
		 */
		public int getColumnsCount() {
			return getObject().getColumns();
		}
	}
	@ShortName("ReadableSheet")
	public static class SheetWrapper extends AbsSheetWrapper<Sheet> {}
	@ShortName("WritableSheet")
	public static class WritableSheetWrapper extends AbsSheetWrapper<WritableSheet> {
		/**
		 * Adds a new cell to the sheet.
		 */
		public void AddCell(WritableCell Cell) throws RowsExceededException, WriteException {
			getObject().addCell(Cell);
		}
		/**
		 * Sets the sheet name.
		 */
		public void setName(String v) {
			getObject().setName(v);
		}
		public void SetRowHeight(int Row, float Height) throws RowsExceededException {
			getObject().setRowView(Row, (int)(Height * 20));
		}
		public void SetColumnWidth(int Col, int Width) {
			getObject().setColumnView(Col, Width);
		}
	}
	@ShortName("WritableCell")
	public static class WritableCellWrapper extends AbsObjectWrapper<WritableCell> {
		/**
		 * Creates a new WritableCell in the given coordinates and text value.
		 *Call WritableSheet.AddCell to add it to the sheet.
		 */
		public void InitializeText(int Col, int Row, String Value) {
			Label l = new Label(Col, Row, Value);
			setObject(l);
		}
		/**
		 * Creates a new WritableCell in the given coordinates and number value.
		 *Call WritableSheet.AddCell to add it to the sheet.
		 */
		public void InitializeNumber(int Col, int Row, double Value) {
			jxl.write.Number n = new jxl.write.Number(Col, Row, Value);
			setObject(n);
		}
		/**
		 * Creates a new WritableCell in the given coordinates and formula.
		 *Call WritableSheet.AddCell to add it to the sheet.
		 *<code>
		 *cell.InitializeFormula(0, 1, "34 + 34")
		 *sheet1.AddCell(cell)</code>
		 */
		public void InitializeFormula(int Col, int Row, String Formula) {
			Formula f = new Formula(Col, Row, Formula);
			setObject(f);
		}
		/**
		 * Sets the cell format. Note that you can use the same WritableCellFormat for multiple cells.
		 */
		public void SetCellFormat(WritableCellFormatWrapper CellFormat ) {
			getObject().setCellFormat(CellFormat.getObject());
		}
	}

	@ShortName("WritableCellFormat")
	public static class WritableCellFormatWrapper extends AbsObjectWrapper<WritableCellFormat> {
		  public final static Colour COLOR_UNKNOWN      = Colour.UNKNOWN;
		  public final static Colour COLOR_BLACK        = Colour.BLACK;
		  public final static Colour COLOR_WHITE        = Colour.WHITE;
		  public final static Colour COLOR_RED                  = Colour.RED      ;    
		  public final static Colour COLOR_BRIGHT_GREEN         = Colour.BRIGHT_GREEN;    
		  public final static Colour COLOR_BLUE                 = Colour.BLUE         ;   
		  public final static Colour COLOR_PINK                 = Colour.PINK          ;  
		  public final static Colour COLOR_TURQUOISE            = Colour.TURQUOISE      ; 
		  public final static Colour COLOR_DARK_RED             = Colour.DARK_RED        ;
		  public final static Colour COLOR_GREEN                = Colour.GREEN           ;
		  public final static Colour COLOR_DARK_BLUE            = Colour.DARK_BLUE       ;
		  public final static Colour COLOR_DARK_YELLOW          = Colour.DARK_YELLOW     ;
		  public final static Colour COLOR_VIOLET               = Colour.VIOLET          ;
		  public final static Colour COLOR_TEAL                 = Colour.TEAL            ;
		  public final static Colour COLOR_GREY_25_PERCENT      = Colour.GREY_25_PERCENT ;
		  public final static Colour COLOR_GREY_50_PERCENT      = Colour.GREY_50_PERCENT ;
		  public final static Colour COLOR_PERIWINKLE           = Colour.PERIWINKLE      ;
		  public final static Colour COLOR_PLUM2                = Colour.PLUM2           ;
		  public final static Colour COLOR_IVORY                = Colour.IVORY           ;
		  public final static Colour COLOR_LIGHT_TURQUOISE2     = Colour.LIGHT_TURQUOISE2;
		  public final static Colour COLOR_DARK_PURPLE          = Colour.DARK_PURPLE     ;
		  public final static Colour COLOR_CORAL                = Colour.CORAL           ;
		  public final static Colour COLOR_OCEAN_BLUE           = Colour.OCEAN_BLUE      ;
		  public final static Colour COLOR_ICE_BLUE             = Colour.ICE_BLUE        ;
		  public final static Colour COLOR_DARK_BLUE2           = Colour.DARK_BLUE2      ;
		  public final static Colour COLOR_PINK2                = Colour.PINK2           ;
		  public final static Colour COLOR_YELLOW2              = Colour.YELLOW2         ;
		  public final static Colour COLOR_TURQOISE2            = Colour.TURQOISE2       ;
		  public final static Colour COLOR_VIOLET2              = Colour.VIOLET2         ;
		  public final static Colour COLOR_DARK_RED2            = Colour.DARK_RED2       ;
		  public final static Colour COLOR_TEAL2                = Colour.TEAL2           ;
		  public final static Colour COLOR_BLUE2                = Colour.BLUE2           ;
		  public final static Colour COLOR_SKY_BLUE             = Colour.SKY_BLUE        ;
		  public final static Colour COLOR_LIGHT_TURQUOISE      = Colour.LIGHT_TURQUOISE ;
		  public final static Colour COLOR_LIGHT_GREEN          = Colour.LIGHT_GREEN     ;
		  public final static Colour COLOR_VERY_LIGHT_YELLOW     = Colour.VERY_LIGHT_YELLOW;
		  public final static Colour COLOR_PALE_BLUE            = Colour.PALE_BLUE       ;
		  public final static Colour COLOR_ROSE                 = Colour.ROSE            ;
		  public final static Colour COLOR_LAVENDER             = Colour.LAVENDER        ;
		  public final static Colour COLOR_TAN                  = Colour.TAN             ;
		  public final static Colour COLOR_LIGHT_BLUE           = Colour.LIGHT_BLUE      ;
		  public final static Colour COLOR_AQUA                 = Colour.AQUA            ;
		  public final static Colour COLOR_LIME                 = Colour.LIME            ;
		  public final static Colour COLOR_GOLD                 = Colour.GOLD            ;
		  public final static Colour COLOR_LIGHT_ORANGE         = Colour.LIGHT_ORANGE    ;
		  public final static Colour COLOR_ORANGE               = Colour.ORANGE          ;
		  public final static Colour COLOR_BLUE_GREY            = Colour.BLUE_GREY       ;
		  public final static Colour COLOR_GREY_40_PERCENT      = Colour.GREY_40_PERCENT ;
		  public final static Colour COLOR_DARK_TEAL            = Colour.DARK_TEAL       ;
		  public final static Colour COLOR_SEA_GREEN            = Colour.SEA_GREEN       ;
		  public final static Colour COLOR_DARK_GREEN           = Colour.DARK_GREEN      ;
		  public final static Colour COLOR_OLIVE_GREEN          = Colour.OLIVE_GREEN     ;
		  public final static Colour COLOR_BROWN                = Colour.BROWN           ;
		  public final static Colour COLOR_PLUM                 = Colour.PLUM            ;
		  public final static Colour COLOR_INDIGO               = Colour.INDIGO          ;
		  public final static Colour COLOR_GREY_80_PERCENT      = Colour.GREY_80_PERCENT ;
		  public final static Colour COLOR_AUTOMATIC            = Colour.AUTOMATIC;
		  public static Alignment HALIGN_GENERAL = Alignment.GENERAL;
		  public static Alignment HALIGN_LEFT    =  Alignment.LEFT;
		  public static Alignment HALIGN_CENTRE  = Alignment.CENTRE;
		  public static Alignment HALIGN_RIGHT   =  Alignment.RIGHT;
		  public static Alignment HALIGN_FILL    = Alignment.FILL;
		  public static Alignment HALIGN_JUSTIFY = Alignment.JUSTIFY;
		  public static VerticalAlignment VALIGN_TOP     = VerticalAlignment.TOP;
		  public static VerticalAlignment VALIGN_BOTTOM     = VerticalAlignment.BOTTOM;
		  public static VerticalAlignment VALIGN_JUSTIFY     = VerticalAlignment.JUSTIFY;
		  public static VerticalAlignment VALIGN_CENTRE    = VerticalAlignment.CENTRE;
		  public static final FontName FONT_ARIAL = WritableFont.ARIAL;
		  public static final FontName FONT_TIMES =  WritableFont.TIMES;
		  public static final FontName FONT_COURIER =  WritableFont.COURIER;
		  public static final FontName FONT_TAHOMA =  WritableFont.TAHOMA;
		  
		    public final static BorderLineStyle BORDER_STYLE_NONE             =        BorderLineStyle.NONE;
			public final static BorderLineStyle BORDER_STYLE_THIN             =        BorderLineStyle.THIN;
			public final static BorderLineStyle BORDER_STYLE_MEDIUM           =        BorderLineStyle.MEDIUM;
			public final static BorderLineStyle BORDER_STYLE_DASHED           =        BorderLineStyle.DASHED;
			public final static BorderLineStyle BORDER_STYLE_DOTTED           =        BorderLineStyle.DOTTED;
			public final static BorderLineStyle BORDER_STYLE_THICK            =        BorderLineStyle.THICK;
			public final static BorderLineStyle BORDER_STYLE_DOUBLE           =        BorderLineStyle.DOUBLE;
			public final static BorderLineStyle BORDER_STYLE_HAIR             =        BorderLineStyle.HAIR;
			public final static BorderLineStyle BORDER_STYLE_MEDIUM_DASHED    =        BorderLineStyle.MEDIUM_DASHED ;
			public final static BorderLineStyle BORDER_STYLE_DASH_DOT         =        BorderLineStyle.DASH_DOT     ;
			public final static BorderLineStyle BORDER_STYLE_MEDIUM_DASH_DOT  =        BorderLineStyle.MEDIUM_DASH_DOT;
			public final static BorderLineStyle BORDER_STYLE_DASH_DOT_DOT     =        BorderLineStyle.DASH_DOT_DOT;
			public final static BorderLineStyle BORDER_STYLE_MEDIUM_DASH_DOT_DOT =     BorderLineStyle.MEDIUM_DASH_DOT_DOT;
			public final static BorderLineStyle BORDER_STYLE_SLANTED_DASH_DOT     =    BorderLineStyle.SLANTED_DASH_DOT;
			
			 public final static Border BORDER_NONE   =  Border.NONE;
			  public final static Border BORDER_ALL    = Border.ALL;
			  public final static Border BORDER_TOP    = Border.TOP;
			  public final static Border BORDER_BOTTOM = Border.BOTTOM;
			  public final static Border BORDER_LEFT   = Border.LEFT;
			  public final static Border BORDER_RIGHT  = Border.RIGHT;
		  /**
		   * Creates a new WritableCellFormat with the default settings.
		   */
		  public void Initialize() {
			  WritableCellFormat c = new WritableCellFormat();
			  setObject(c);
		  }
		  /**
		   * Creates a new WritableCellFormat with the specified font style.
		   *Font - One of the FONT constants.
		   *FontSize - Default value is 10.
		   *Bold - Whether the text should be bold.
		   *Underline - Whether the text should be underlined.
		   *Italic - Whether the text style should be italic.
		   *FontColor - One of the COLOR constants.
		   */
		  public void Initialize2(FontName Font, int FontSize, boolean Bold, boolean Underline, boolean Italic, Colour FontColor) {
			  WritableFont f = new WritableFont(Font, FontSize, Bold ? WritableFont.BOLD : WritableFont.NO_BOLD, 
					  Italic, Underline ? UnderlineStyle.SINGLE : UnderlineStyle.NO_UNDERLINE, FontColor);
			  WritableCellFormat c = new WritableCellFormat(f);
			  setObject(c);
		  }
		  /**
		   * Sets the cell's background color. The color should be one of the COLOR constants.
		   */
		  public void setBackgroundColor(Colour Clr) throws WriteException {
			  getObject().setBackground(Clr);
		  }
		  /**
		   * Sets the cell's horizontal alignment. The value should be one of the HALIGN constants.
		   */
		  public void setHorizontalAlignment(Alignment v) throws WriteException {
			  getObject().setAlignment(v);
		  }
		  /**
		   * Sets the cell's vertical alignment. The value should be one of the VALIGN constants.
		   */
		  public void setVertivalAlignment(VerticalAlignment v) throws WriteException {
			  getObject().setVerticalAlignment(v);
		  }
		  /**
		   * Sets the cell's border.
		   *Border - One of the BORDER constants.
		   *Style - One of the BORDER_STYLE constants.
		   *BorderColor - One of the COLOR constants.
		   */
		  public void SetBorder(Border Border, BorderLineStyle Style, Colour BorderColor) throws WriteException {
			  getObject().setBorder(Border, Style, BorderColor);
		  }
		    
		  

	}
}
