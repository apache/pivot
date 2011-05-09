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
package org.apache.pivot.wtk;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.media.Movie;


/**
 * Component that plays a movie.
 */
@DefaultProperty("movie")
public class MovieView extends Component {
    private static class MovieViewListenerList extends WTKListenerList<MovieViewListener>
        implements MovieViewListener {
        @Override
        public void movieChanged(MovieView movieView, Movie previousMovie) {
            for (MovieViewListener listener : this) {
                listener.movieChanged(movieView, previousMovie);
            }
        }
    }

    private Movie movie = null;

    private MovieViewListenerList movieViewListeners = new MovieViewListenerList();

    /**
     * Creates an empty movie view.
     */
    public MovieView() {
        this(null);
    }

    /**
     * Creates an movie view with the given movie.
     *
     * @param movie
     * The initial movie to set, or <tt>null</tt> for no movie.
     */
    public MovieView(Movie movie) {
        setMovie(movie);

        installSkin(MovieView.class);
    }

    /**
     * Returns the movie view's current movie.
     *
     * @return
     * The current movie, or <tt>null</tt> if no movie is set.
     */
    public Movie getMovie() {
        return movie;
    }

    /**
     * Sets the movie view's current movie.
     *
     * @param movie
     * The movie to set, or <tt>null</tt> for no movie.
     */
    public void setMovie(Movie movie) {
        Movie previousMovie = this.movie;

        if (previousMovie != movie) {
            this.movie = movie;
            movieViewListeners.movieChanged(this, previousMovie);
        }
    }

    /**
     * Returns the movie view listener list.
     *
     * @return
     * The movie view listener list.
     */
    public ListenerList<MovieViewListener> getMovieViewListeners() {
        return movieViewListeners;
    }
}
