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

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;

/**
 * Creates a selection rectangle based on mouse input.
 * Causes zoom to selection (an approximation).
 * Also triggers repaint events in the viewer.
 *
 * @author Martin Steiger
 * @author Derek Weber
 * @see <A HREF="https://github.com/msteiger/jxmapviewer2/blob/master/examples/src/sample3_interaction/SelectionAdapter.java">SelectionAdapter.java</A>
 */
public class SelectionAdapter extends MouseAdapter 
{
	private boolean dragging;
	private JXMapViewer viewer;

	private Point2D startPos = new Point2D.Double();
	private Point2D endPos = new Point2D.Double();

	/**
	 * @param viewer the jxmapviewer
	 */
	public SelectionAdapter(JXMapViewer viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.getButton() != MouseEvent.BUTTON3)
			return;
		
		startPos.setLocation(e.getX(), e.getY());
		endPos.setLocation(e.getX(), e.getY());
		
		dragging = true;
	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (!dragging)
			return;
		
		endPos.setLocation(e.getX(), e.getY());
		
		viewer.repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (!dragging)
			return;
		
		if (e.getButton() != MouseEvent.BUTTON3)
			return;

		final Rectangle rectangle = getRectangle();

        final double newCentreX = rectangle.x + rectangle.width / 2.0;
        final double newCentreY = rectangle.y + rectangle.height / 2.0;

        final GeoPosition newCentre = viewer.convertPointToGeoPosition(
            new Point2D.Double(newCentreX, newCentreY)
        );

        viewer.setCenterPosition(newCentre);
        viewer.setZoom(viewer.getZoom() - 1); // not sure how far to zoom in

		viewer.repaint();
		
		dragging = false;
	}

	/**
	 * @return the selection rectangle
	 */
	public Rectangle getRectangle()
	{
		if (dragging)
		{
			int x1 = (int) Math.min(startPos.getX(), endPos.getX());
			int y1 = (int) Math.min(startPos.getY(), endPos.getY());
			int x2 = (int) Math.max(startPos.getX(), endPos.getX());
			int y2 = (int) Math.max(startPos.getY(), endPos.getY());
			
			return new Rectangle(x1, y1, x2-x1, y2-y1);
		}
		
		return null;
	}

}