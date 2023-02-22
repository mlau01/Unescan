package unescan.gui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import unescan.Unescan;

public class Ressource {
	
	private final Win win;
	private final ArrayList<Color> colors;
	private final ArrayList<Image> images;
	private final ArrayList<Font> fonts;
	
	public static final int COLOR_SELECTION = 0;
	public static final int COLOR_RED = 1;
	public static final int COLOR_REDRED = 2;
	public static final int COLOR_BLUE = 3;
	public static final int COLOR_GREEN = 4;
	public static final int IMAGE_USC1 = 0;
	public static final int IMAGE_USC2 = 1;
	public static final int IMAGE_USC3 = 2;
	public static final int IMAGE_USC4 = 3;
	public static final int IMAGE_SAVE = 4;
	public static final int IMAGE_ICON = 5;
	public static final int FONT_BARCODE = 0;
	public static final int FONT_LOCATION = 1;
	public static final int FONT_SAVESTATUS = 2;
	
	public Ressource(final Win p_win)
	{
		
		win = p_win;
		colors = new ArrayList<Color>();
		images = new ArrayList<Image>();
		fonts = new ArrayList<Font>();
		
		loadColor(COLOR_SELECTION, 110, 160, 255);
		loadColor(COLOR_RED, 252, 218, 220);
		loadColor(COLOR_REDRED, 255, 0, 0);
		loadColor(COLOR_BLUE, 230, 240, 255);
		loadColor(COLOR_GREEN, 141, 236, 120);
		loadImage(IMAGE_USC1, "Usc-1.png");
		loadImage(IMAGE_USC2, "Usc-2.png");
		loadImage(IMAGE_USC3, "Usc-3.png");
		loadImage(IMAGE_USC4, "Usc-4.png");
		loadImage(IMAGE_SAVE, "Save-icon_35px.png");
		loadImage(IMAGE_ICON, "Www_48x48.png");
		loadFont(FONT_BARCODE, "verdana", 12, SWT.BOLD);
		loadFont(FONT_LOCATION, "verdana", 18, SWT.NORMAL);
		loadFont(FONT_SAVESTATUS, "verdana", 14, SWT.BOLD);
	}
	
	public void loadColor(final int index, int r, int g, int b)
	{
		colors.add(index, new Color(win.getDisplay(), r, g, b));
	}
	
	public final Color getColor(final int index)
	{
		return colors.get(index);
	}
	
	public void loadImage(final int index, final String imageName)
	{
		InputStream imgIs = this.getClass().getResourceAsStream(Unescan.IMAGE_PATH + imageName);
		images.add(index, new Image(win.getDisplay(), imgIs));
		
		try {
			imgIs.close();
		} catch (IOException e) {
			win.showError(e.getClass().getName(), e.getMessage());
			e.printStackTrace();
		}
	}
	
	public final Image getImage(int index)
	{
		return images.get(index);
	}
	
	public void loadFont(final int index, final String name, final int size, final int style)
	{
		fonts.add(index, new Font(win.getDisplay(), name, size, style));
	}
	public final Font getFont(int index)
	{
		return fonts.get(index);
	}
	
	public void disposeAll()
	{
		for(Color c : colors)
		{
			if(c != null) c.dispose();
		}
		for(Image i : images)
		{
			if(i != null) i.dispose();
		}
		for(Font f : fonts)
		{
			if(f != null) f.dispose();
		}
	}


}
