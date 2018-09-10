/*
 * Copyright 2017 Derek Weber
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.org.dcw.socialmedia.simulation.tools.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import org.jxmapviewer.painter.Painter;

/**
 * Paints a selection rectangle and a central icon.
 * @author Martin Steiger
 * @author Derek Weber
 * @see <A HREF="https://github.com/msteiger/jxmapviewer2/blob/master/examples/src/sample3_interaction/SelectionPainter.java">SelectionPainter.java</A>
 */
public class SelectionPainter implements Painter<Object>
{
	private Color fillColor = new Color(128, 192, 255, 128);
	private Color frameColor = new Color(0, 0, 255, 128);

	private SelectionAdapter adapter;
	
	/**
	 * @param adapter the selection adapter
	 */
	public SelectionPainter(SelectionAdapter adapter)
	{
		this.adapter = adapter;
	}

	@Override
	public void paint(Graphics2D g, Object t, int width, int height)
	{
		Rectangle rc = adapter.getRectangle();
		
		if (rc != null)
		{
			g.setColor(frameColor);
			g.draw(rc);
			g.setColor(fillColor);
			g.fill(rc);
		}

		final int midX = width / 2;
		final int midY = height / 2;

		final int crossSize = 5;
		final int iconSize = crossSize * 4;

		g.setColor(frameColor);
		g.setStroke(new BasicStroke(2));
		g.drawLine(midX, midY - crossSize, midX, midY + crossSize);
        g.drawLine(midX - crossSize, midY, midX + crossSize, midY);
        g.drawRect(midX - iconSize / 2, midY - iconSize / 2, iconSize, iconSize);
	}
}