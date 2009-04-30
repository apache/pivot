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
package pivot.wtk.skin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.MovieView;
import pivot.wtk.MovieViewListener;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Movie;
import pivot.wtk.media.MovieListener;

/**
 * Movie view skin.
 *
 * @author tvolkert
 */
public class MovieViewSkin extends ComponentSkin implements MovieViewListener {
    private Color backgroundColor = null;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    private MovieListener movieListener = new MovieListener.Adapter() {
        @Override
        public void regionUpdated(Movie movie, int x, int y, int width, int height) {
            // TODO This is wrong, considering alignment values
            repaintComponent(x, y, width, height);
        }
    };

    public void install(Component component) {
        super.install(component);

        MovieView movieView = (MovieView)component;
        movieView.getMovieViewListeners().add(this);

        Movie movie = movieView.getMovie();
        if (movie != null) {
            movie.getMovieListeners().add(movieListener);
        }
    }

    public void uninstall() {
        MovieView movieView = (MovieView)getComponent();
        Movie movie = movieView.getMovie();
        if (movie != null) {
            movie.getMovieListeners().remove(movieListener);
        }

        movieView.getMovieViewListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        MovieView movieView = (MovieView)getComponent();
        Movie movie = movieView.getMovie();

        return (movie == null) ? 0 : movie.getWidth();
    }

    public int getPreferredHeight(int width) {
        MovieView movieView = (MovieView)getComponent();
        Movie movie = movieView.getMovie();

        return (movie == null) ? 0 : movie.getHeight();
    }

    public Dimensions getPreferredSize() {
        MovieView movieView = (MovieView)getComponent();
        Movie movie = movieView.getMovie();

        return (movie == null) ? new Dimensions(0, 0) : movie.getSize();
    }

    public void layout() {
        // No-op for component skins
    }

    public void paint(Graphics2D graphics) {
        MovieView movieView = (MovieView)getComponent();
        Movie movie = movieView.getMovie();

        int width = getWidth();
        int height = getHeight();

        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        if (movie != null) {
            Dimensions movieSize = movie.getSize();

            int movieX, movieY;

            switch (horizontalAlignment) {
            case CENTER:
                movieX = (width - movieSize.width) / 2;
                break;
            case RIGHT:
                movieX = width - movieSize.width;
                break;
            default:
                movieX = 0;
                break;
            }

            switch (verticalAlignment) {
            case CENTER:
                movieY = (height - movieSize.height) / 2;
                break;
            case BOTTOM:
                movieY = height - movieSize.height;
                break;
            default:
                movieY = 0;
                break;
            }

            graphics.translate(movieX, movieY);
            movie.paint(graphics);
        }
    }

    /**
     * @return
     * <tt>false</tt>; movie views are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    /**
     * A movie view's background color dictates whether or not it's opaque.
     */
    @Override
    public boolean isOpaque() {
        boolean opaque = false;

        if (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE) {
            opaque = true;
        }

        return opaque;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(decodeColor(backgroundColor));
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
            throw new IllegalArgumentException("JUSTIFY alignment is not supported");
        }

        this.horizontalAlignment = horizontalAlignment;
        repaintComponent();
    }

    public final void setHorizontalAlignment(String horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        setHorizontalAlignment(HorizontalAlignment.decode(horizontalAlignment));
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        if (verticalAlignment == VerticalAlignment.JUSTIFY) {
            throw new IllegalArgumentException("JUSTIFY alignment is not supported");
        }

        this.verticalAlignment = verticalAlignment;
        repaintComponent();
    }

    public final void setVerticalAlignment(String verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        setVerticalAlignment(VerticalAlignment.decode(verticalAlignment));
    }

    // MovieViewListener methods

    public void movieChanged(MovieView movieView, Movie previousMovie) {
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
