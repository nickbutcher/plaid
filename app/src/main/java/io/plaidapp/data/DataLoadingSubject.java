package io.plaidapp.data;

/**
 * An interface for classes offering data loading state to be observed
 */
public interface DataLoadingSubject {
    boolean isDataLoading();
}
