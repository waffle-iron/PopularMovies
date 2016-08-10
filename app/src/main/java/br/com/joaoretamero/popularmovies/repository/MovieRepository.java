package br.com.joaoretamero.popularmovies.repository;

import br.com.joaoretamero.popularmovies.model.Movie;
import io.realm.RealmResults;
import io.realm.Sort;

public class MovieRepository extends BaseRealmRepository<Movie> {

    public MovieRepository() {
        super(Movie.class);
    }

    public Movie findById(int id) {
        return realm.where(Movie.class).equalTo("id", id).findFirst();
    }

    public RealmResults<Movie> findAllSortByVote() {
        return this.findAll().sort("voteAverage", Sort.DESCENDING);
    }
}