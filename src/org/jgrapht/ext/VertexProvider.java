package org.jgrapht.ext;

/* ==========================================
 * JGraphT : a free Java graph-theory library
 * ==========================================
 *
 * Project Info:  http://jgrapht.sourceforge.net/
 * Project Creator:  Barak Naveh (http://sourceforge.net/users/barak_naveh)
 *
 * (C) Copyright 2003-2008, by Barak Naveh and Contributors.
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
/* ------------------
 * VertexProvider.java
 * ------------------
 * (C) Copyright 2015, by  Wil Selwood.
 *
 * Original Author:  Wil Selwood <wselwood@ijento.com>
 *
 */

import java.util.*;


/**
 * Creates a Vertex of type V
 *
 * @param <V> Vertex type
 */
public interface VertexProvider<V>
{
    /**
     * Create a vertex
     *
     * @param label the label of the vertex
     * @param attributes any other attributes of the vertex
     *
     * @return the vertex.
     */
    V buildVertex(String label, Map<String, String> attributes);
}

// End VertexProvider.java