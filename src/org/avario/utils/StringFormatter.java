package org.avario.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.content.Context;
import android.graphics.Typeface;

public class StringFormatter {
	private static Typeface largeFont;
	private static final DecimalFormat twoDecimalsFormatter = new DecimalFormat("0.00");
	private static final NumberFormat noDecimalsFormatter = new DecimalFormat("#");
	private static final DecimalFormat oneDecimalFormatter = new DecimalFormat("0.0");
	private static final DecimalFormat multipleDecimalFormatter = new DecimalFormat("0.000000000000000");

	
	public static String twoDecimals(double value) {
		return twoDecimalsFormatter.format(value);
	}

	public static String multipleDecimals(float value) {
		return multipleDecimalFormatter.format(value);
	}
	
	public static String twoDecimals(float value) {
		return twoDecimalsFormatter.format(value);
	}

	public static String noDecimals(double value) {
		return noDecimalsFormatter.format(value);
	}

	public static String noDecimals(float value) {
		return noDecimalsFormatter.format(value);
	}

	public static String oneDecimal(float value) {
		return oneDecimalFormatter.format(value);
	}

	public static Typeface getLargeFont(Context context) {
		if (largeFont == null) {
			largeFont = Typeface.createFromAsset(context.getAssets(), "fonts/Patagonia.ttf");
		}
		return largeFont;
	}

}
