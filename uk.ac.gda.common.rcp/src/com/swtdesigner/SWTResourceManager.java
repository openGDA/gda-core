
package com.swtdesigner;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 * Utility class for managing OS resources associated with SWT controls such as colors, fonts, images, etc.
 * <p>
 * <b>IMPORTANT:</b> Application code must explicitly invoke the <code>dispose()</code> method to release the
 * operating system resources managed by cached objects when those objects and OS resources are no longer
 * needed (e.g. on application shutdown)
 * <p>
 * This class may be freely distributed as part of any application or plugin.
 * <p>
 * Copyright (c) 2003 - 2007, Instantiations, Inc. <br>
 * All Rights Reserved
 *
 * @author scheglov_ke
 * @author Dan Rubel
 * @author (DLS modifications 2024) P. A. Coe
 */
public class SWTResourceManager {

	/**
	 * Style constant for placing decorator image in top left corner of base image.
	 */
	public static final int TOP_LEFT = 1;
	/**
	 * Style constant for placing decorator image in top right corner of base image.
	 */
	public static final int TOP_RIGHT = 2;
	/**
	 * Style constant for placing decorator image in bottom left corner of base image.
	 */
	public static final int BOTTOM_LEFT = 3;
	/**
	 * Style constant for placing decorator image in bottom right corner of base image.
	 */
	public static final int BOTTOM_RIGHT = 4;


	/**
	 * Internal value.
	 */
	protected static final int LAST_CORNER_KEY = 5;

	private static final int MISSING_IMAGE_SIZE = 10;

	/**
	 * Maps font names to fonts.
	 */
	private static final Map<String, Font> FONT_MAP = new HashMap<>();

	/**
	 * Maps fonts to their bold versions.
	 */
	private static final Map<Font, Font> INTERNAL_BOLD_FONT_MAP = new HashMap<>();

	/**
	 * Maps images to decorated images.
	 */

	private static final Map<RGB, Color> COLOR_MAP = new HashMap<>();
	private static final Map<String, Image> IMAGE_MAP = new HashMap<>();
	private static final Map<Integer, Cursor> CURSOR_MAP = new HashMap<>();

	private static final Map<Integer, Map<Image, Map<Image, Image>>> DECORATED_IMAGE_MAP = new HashMap<>();

	/**
	 * Dispose of cached objects and their underlying OS resources.
	 * This should only be called when the cached
	 * objects are no longer needed (e.g. on application shutdown).
	 */
	public static void dispose() {
		disposeColors();
		disposeImages();
		disposeFonts();
		disposeCursors();
	}

	/**
	 * Returns the system {@link Color} matching the specific ID.
	 * @param systemColorID the ID value for the color
	 * @return the system {@link Color} matching the specific ID
	 */
	public static Color getColor(int systemColorID) {
		var display = Display.getCurrent();
		return display.getSystemColor(systemColorID);
	}

	/**
	 * Returns a {@link Color} given its red, green and blue component values.
	 * @param r the red component of the color
	 * @param g the green component of the color
	 * @param b the blue component of the color
	 * @return the {@link Color} matching the given red, green and blue component values
	 */
	public static Color getColor(int r, int g, int b) {
		var rgb = new RGB(r, g, b);
		return getColor(rgb);
	}

	/**
	 * Returns a {@link Color} given its RGB value.
	 * @param rgb the {@link RGB} value of the color
	 * @return the {@link Color} matching the RGB value
	 */
	public static Color getColor(RGB rgb) {
		Function<RGB, Color> fallbackSupplier = r -> {
			var display = Display.getCurrent();
			return new Color(display, r);
		};
		return COLOR_MAP.computeIfAbsent(rgb, fallbackSupplier);
	}

	/**
	 * Dispose of all the cached {@link Color}'s.
	 */
	public static void disposeColors() {
		clearOutMap(COLOR_MAP, Color::dispose);
	}

	/**
	 * Returns an {@link Image} encoded by the specified {@link InputStream}, which is not closed here.
	 * @param stream the {@link InputStream} encoding the image data
	 * @return the {@link Image} encoded by the specified input stream
	 */
	protected static Image getImage(InputStream stream) {
		var display = Display.getCurrent();
		var imageData = new ImageData(stream);
		if (imageData.transparentPixel > 0) {
			var transparencyMask = imageData.getTransparencyMask();
			return new Image(display, imageData, transparencyMask);
		}
		return new Image(display, imageData);
	}

	/**
	 * Returns an {@link Image} stored in the file at the specified path.
	 *
	 * @param path
	 *            the path to the image file
	 * @return the {@link Image} stored in the file at the specified path
	 */
	public static Image getImage(String path) {
		return IMAGE_MAP.computeIfAbsent( path, p -> imageFromStream(p, FileInputStream::new) );
	}

	/**
	 * Returns an {@link Image} stored in the file at the specified path relative to the specified class.
	 *
	 * @param clazz the {@link Class} relative to which to find the image
	 * @param path the path to the image file, if starts with <code>'/'</code>
	 * @return the {@link Image} stored in the file at the specified path
	 */
	public static Image getImage(Class<?> clazz, String path) {
		ThrowingFunction<?, String, InputStream> streamProvider =
			p -> {
				var loader = clazz.getClassLoader().getResourceAsStream(p);
				return new BufferedInputStream(loader);
			};
		var key = clazz.getName() + '|' + path;
		return IMAGE_MAP.computeIfAbsent(key, ignoredKey -> imageFromStream(path, streamProvider));
	}

	private static <X extends Exception> Image imageFromStream(String path,
																ThrowingFunction<X, String, InputStream> streamSource) {
		try(InputStream inputStream = streamSource.applyWithException(path)) {
			return getImage(inputStream);
		} catch (Exception e) {
			return getMissingImage();
		}
	}

	/**
	 * @return the small {@link Image} that can be used as placeholder for missing image.
	 */
	private static Image getMissingImage() {
		var display = Display.getCurrent();
		var image = new Image(display, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
		var gc = new GC(image);
		gc.setBackground(getColor(SWT.COLOR_RED));
		gc.fillRectangle(0, 0, MISSING_IMAGE_SIZE, MISSING_IMAGE_SIZE);
		gc.dispose();
		return image;
	}

	/**
	 * Returns an {@link Image} composed of a base image decorated by another image.
	 *
	 * @param baseImage
	 *            the base {@link Image} that should be decorated
	 * @param decorator
	 *            the {@link Image} to decorate the base image
	 * @return {@link Image} The resulting decorated image
	 */
	public static Image decorateImage(Image baseImage, Image decorator) {
		return decorateImage(baseImage, decorator, BOTTOM_RIGHT);
	}

	/**
	 * Returns an {@link Image} composed of a base image decorated by another image.
	 *
	 * @param baseImage
	 *            the base {@link Image} that should be decorated
	 * @param decorator
	 *            the {@link Image} to decorate the base image
	 * @param corner
	 *            the corner to place decorator image
	 * @return the resulting decorated {@link Image}
	 */
	public static Image decorateImage(Image baseImage, Image decorator, int corner) {
		verifyCorner(corner);
		Map<Image, Map<Image, Image>> cornerDecoratedImageMap = DECORATED_IMAGE_MAP.computeIfAbsent(corner, c -> new HashMap<>());
		Map<Image, Image> decoratedMap = cornerDecoratedImageMap.computeIfAbsent(baseImage, im -> new HashMap<>());
		return decoratedMap.computeIfAbsent(decorator, j-> createCornerImage(baseImage, decorator, corner));
	}

	private static Image createCornerImage(Image baseImage, Image decorator, int corner) {
		var baseImageBounds = baseImage.getBounds();
		var decoratorBounds = decorator.getBounds();
		var display = Display.getCurrent();
		var image = new Image(display, baseImageBounds.width, baseImageBounds.height);
		var gc = new GC(image);
		gc.drawImage(baseImage, 0, 0);
		switch(corner) {
			case TOP_LEFT: {
				gc.drawImage(decorator, 0, 0);
				break;
			}
			case TOP_RIGHT: {
				gc.drawImage(decorator, widthDifferenceBetween(baseImageBounds,decoratorBounds), 0);
				break;
			}
			case BOTTOM_LEFT: {
				gc.drawImage(decorator, 0, heightDifferenceBetween(baseImageBounds,decoratorBounds));
				break;
			}
			case BOTTOM_RIGHT: {
				gc.drawImage(decorator, widthDifferenceBetween(baseImageBounds,decoratorBounds), heightDifferenceBetween(baseImageBounds,decoratorBounds));
				break;
			}
			default: break;
		}
		gc.dispose();
		return image;
	}

	private static int heightDifferenceBetween(Rectangle a, Rectangle b) {
		return a.height - b.height;
	}

	private static int widthDifferenceBetween(Rectangle a, Rectangle b) {
		return a.width - b.width;
	}

	private static void verifyCorner(int corner) {
		var lowestPermittedCorner = 1;
		var highestPermittedCorner = LAST_CORNER_KEY-1;
		if (corner < lowestPermittedCorner || corner > highestPermittedCorner) {
			throw new IllegalArgumentException("Wrong decorate corner");
		}
	}

	/**
	 * Dispose all of the cached {@link Image}'s.
	 */
	public static void disposeImages() {
		disposeLoadedImages();
		disposeDecoratedImages();
	}

	private static void disposeLoadedImages() {
		clearOutMap(IMAGE_MAP, Image::dispose);
	}

	private static void disposeDecoratedImages() {
		Consumer<Map<Image,Image>> disposeOfInnerMap = m -> clearOutMap(m, Image::dispose);
		DECORATED_IMAGE_MAP.values()
				.stream()
				.filter(Objects::nonNull)
				.forEach( d -> clearOutMap(d, disposeOfInnerMap));
		DECORATED_IMAGE_MAP.clear();
	}

	/**
	 * Returns a {@link Font} based on its name, height and style.
	 * @param name the name of the font
	 * @param height the height of the font
	 * @param style the style of the font
	 * @return {@link Font} The font matching the name, height and style
	 */
	public static Font getFont(String name, int height, int style) {
		String fontName = name + '|' + height + '|' + style + "|false|false";
		return FONT_MAP.computeIfAbsent(fontName, n-> {
			var display = Display.getCurrent();
			var fontData = new FontData(name, height, style);
			return new Font(display, fontData);
		});
	}

	/**
	 * Returns a bold version of the given {@link Font}.
	 *
	 * @param baseFont
	 *            the {@link Font} for which a bold version is desired
	 * @return the bold version of the given {@link Font}
	 */
	public static Font getBoldFont(Font baseFont) {
		UnaryOperator<Font> boldSupplier = b -> {
			var fontDatas = baseFont.getFontData();
			FontData data = fontDatas[0];
			var display = Display.getCurrent();
			var name = data.getName();
			var height = data.getHeight();
			return new Font(display, name, height, SWT.BOLD);
		};
		return INTERNAL_BOLD_FONT_MAP.computeIfAbsent(baseFont, boldSupplier::apply);
	}

	/**
	 * Dispose all of the cached {@link Font}'s.
	 */
	public static void disposeFonts() {
		Consumer<Font> disposeFont = Font::dispose;
		clearOutMap(FONT_MAP, disposeFont);
		clearOutMap(INTERNAL_BOLD_FONT_MAP, disposeFont);
	}

	private static <V> void clearOutMap(Map<?, V> map, Consumer<V> disposeAction) {
		map.values()
			.stream()
			.forEach(disposeAction);
		map.clear();
	}

	/**
	 * Maps IDs to Cursors
	 * Returns the system cursor matching the specific ID
	 * @param id int The ID value for the cursor
	 * @return Cursor The system cursor matching the specific ID
	 */
	public static Cursor getCursor(int id) {
		IntFunction<Cursor> fallbackCursorSupplier = k -> {
			var display = Display.getDefault();
			return new Cursor(display, id);
		};
		return CURSOR_MAP.computeIfAbsent(id, fallbackCursorSupplier::apply);
	}

	/**
	 * Dispose all of the cached cursors.
	 */
	public static void disposeCursors() {
		clearOutMap(CURSOR_MAP, Cursor::dispose);
	}

	@FunctionalInterface
	interface ThrowingFunction<E extends Exception, T, R> {
		R applyWithException(T t) throws E;
	}
}
