package com.MennoSpijker.kentekenscanner.view

interface RepositoryCallback<T>
{
    /**
     * On response
     * @param data The corresponding response
     */
    fun onResponse(data: T)


    /**
     * on failure
     * @param error The request error
     */
    fun onFailure(error: T)
}