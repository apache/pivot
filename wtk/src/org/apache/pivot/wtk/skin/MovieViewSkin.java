/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.skin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.MovieView;
import org.apache.pivot.wtk.MovieViewListener;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Movie;
import org.apache.pivot.wtk.media.MovieListener;

/**
 * Movie view skin.
 */
public class MovieViewSkin extends ComponentSkin implements MovieViewListener {
    private Color backgroundColor = null;
    private float scale = 1;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    private int movieX = 0;
    private int movieY = 0;

    private MovieListener movieListener = new MovieListener() {
        @Override
        public void sizeChanged(final Movie movie, final int previousWidth, final int previousHeight) {
            invalidateComponent();
        }

        @Override
        public void regionUpdated(final Movie movie, final int x, final int y, final int width, final int height) {
            repaintComponent(
                movieX + (int) Math.floor(x * scale),
                movieY + (int) Math.floor(y * scale),
                (int) Math.ceil(width * scale) + 1,
                (int) Math.ceil(height * scale) + 1);
        }
    };

    @Override
    public void install(final Component component) {
        super.install(component);

        MovieView movieView = (MovieView) component;
        movieView.getMovieViewListeners().add(this);

        Movie movie = movieView.getMovie();
        if (movie != null) {
            movie.getMovieListeners().add(movieListener);
        }
    }

    @Override
    public int getPreferredWidth(final int height) {
        MovieView movieView = (MovieView) getComponent();
        Movie movie = movieView.getMovie();

        return (movie == null) ? 0 : Math.round(movie.getWidth() * scale);
    }

    @Override
    public int getPreferredHeight(final int width) {
        MovieView movieView = (MovieView) getComponent();
        Movie movie = movieView.getMovie();

        return (movie == null) ? 0 : Math.round(movie.getHeight() * scale);
    }

    @Override
    public Dimensions getPreferredSize() {
        MovieView movieView = (MovieView) getComponent();
        Movie movie = movieView.getMovie();

        return (movie == null) ? Dimensions.ZERO
            : new Dimensions(Math.round(movie.getWidth() * scale),
                             Math.round(movie.getHeight() * scale));
    }

    @Override
    public void layout() {
        MovieView movieView = (MovieView) getComponent();
        Movie movie = movieView.getMovie();

        if (movie != null) {
            int width = getWidth();
            int height = getHeight();

            int movieWidth = movie.getWidth();
            int movieHeight = movie.getHeight();

            switch (horizontalAlignment) {
                case CENTER:
                    movieX = (width - movieWidth) / 2;
                    break;
                case RIGHT:
                    movieX = width - movieWidth;
                    break;
                default:
                    movieX = 0;
                    break;
            }

            switch (verticalAlignment) {
                case CENTER:
                    movieY = (height - movieHeight) / 2;
                    break;
                case BOTTOM:
                    movieY = height - movieHeight;
                    break;
                default:
                    movieY = 0;
                    break;
            }
        }
    }

    @Override
    public void paint(final Graphics2D graphics) {
        MovieView movieView = (MovieView) getComponent();
        Movie movie = movieView.getMovie();

        int width = getWidth();
        int height = getHeight();

        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        if (movie != null) {
            if (scale != 1) {
                graphics.scale(scale, scale);
            }

            graphics.translate(movieX, movieY);
            movie.paint(graphics);
        }
    }

    /**
     * @return <tt>false</tt>; movie views are not focusable.
     */
    @Override
    public final boolean isFocusable() {
        return false;
    }

    @Override
    public final boolean isOpaque() {
        return (backgroundColor != null && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public final Color getBackgroundColor() {
        return backgroundColor;
    }

    public final void setBackgroundColor(final Color backgroundColor) {
        // Note: null background allowed here
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final float getScale() {
        return scale;
    }

    public final void setScale(final float scale) {
        if (scale <= 0) {
            throw new IllegalArgumentException("scale must be positive.");
        }

        this.scale = scale;
        invalidateComponent();
    }

    public final HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public final void setHorizontalAlignment(final HorizontalAlignment horizontalAlignment) {
        Utils.checkNull(horizontalAlignment, "horizontalAlignment");

        if (this.horizontalAlignment != horizontalAlignment) {
            this.horizontalAlignment = horizontalAlignment;

            layout();
            repaintComponent();
        }
    }

    public final VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public final void setVerticalAlignment(final VerticalAlignment verticalAlignment) {
        Utils.checkNull(verticalAlignment, "verticalAlignment");

        if (this.verticalAlignment != verticalAlignment) {
            this.verticalAlignment = verticalAlignment;

            layout();
            repaintComponent();
        }
    }

    // MovieViewListener methods

    @Override
    public void movieChanged(final MovieView movieView, final Movie previousMovie) {
        if (previousMovie != null) {
            previousMovie.getMovieListeners().remove(movieListener);
        }

        Movie movie = movieView.getMovie();
        if (movie != null) {
            movie.getMovieListeners().add(movieListener);
        }

        invalidateComponent();
    }
}
