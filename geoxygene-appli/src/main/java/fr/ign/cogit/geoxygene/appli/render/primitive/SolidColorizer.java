/*******************************************************************************
 * This file is part of the GeOxygene project source files.
 * 
 * GeOxygene aims at providing an open framework which implements OGC/ISO
 * specifications for the development and deployment of geographic (GIS)
 * applications. It is a open source contribution of the COGIT laboratory at the
 * Institut Géographique National (the French National Mapping Agency).
 * 
 * See: http://oxygene-project.sourceforge.net
 * 
 * Copyright (C) 2005 Institut Géographique National
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library (see file LICENSE if present); if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 *******************************************************************************/

package fr.ign.cogit.geoxygene.appli.render.primitive;

/**
 * @author JeT
 *         this colorizer returns one solid color independently of the position
 */
public class SolidColorizer implements Colorizer {

    float[] rgba = new float[4];

    /**
     * Quick constructor
     * 
     * @param color
     *            color to set
     */
    public SolidColorizer(java.awt.Color color) {
        super();
        this.rgba[0] = (float) (color.getRed() / 255.);
        this.rgba[1] = (float) (color.getGreen() / 255.);
        this.rgba[2] = (float) (color.getBlue() / 255.);
        this.rgba[3] = (float) (color.getAlpha() / 255.);
    }

    /*
     * (non-Javadoc)
     * 
     * @see fr.ign.cogit.geoxygene.appli.render.primitive.Colorizer#
     * initializeColorization()
     */
    @Override
    public void initializeColorization() {
        // nothing to initialize
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ign.cogit.geoxygene.appli.render.primitive.Colorizer#finalizeColorization
     * ()
     */
    @Override
    public void finalizeColorization() {
        // nothing to finalize
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * fr.ign.cogit.geoxygene.appli.render.primitive.Colorizer#getColor(double
     * [])
     */
    @Override
    public float[] getColor(double[] vertex) {
        return this.rgba;
    }

}