package com.example.test.restaurant.ui

import android.content.Context
import android.graphics.Typeface
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.test.restaurant.R
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.data.DataBufferUtils
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.AutocompletePrediction
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.LatLngBounds
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * FragmentAdapter that handles Autocomplete requests from the Places Geo Data API.
 * com.google.android.gms.location.places.AutocompletePrediction results from the API are frozen and stored directly in this
 * adapter. (See com.google.android.gms.location.places.AutocompletePrediction.freeze.)
 *
 * @author Prateek on on 20/07/18.
 */

internal class PlaceAutocompleteAdapter(context: Context, private val mGoogleApiClient: GoogleApiClient,
                                        private var mBounds: LatLngBounds?, private val mPlaceFilter:
                                        AutocompleteFilter) : ArrayAdapter<AutocompletePrediction>
                                        (context, R.layout.auto_complete_item, R.id.text1), Filterable {

    private var mResultList: ArrayList<AutocompletePrediction> = ArrayList()

    override fun getCount(): Int = mResultList.size

    override fun getItem(position: Int): AutocompletePrediction? = mResultList[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = super.getView(position, convertView, parent)

        // Sets the primary and secondary text for a row.
        // Note that getPrimaryText() and getSecondaryText() return a CharSequence that may contain
        // styling based on the given CharacterStyle.

        val item = getItem(position)

        val textView1 = row.findViewById<TextView>(R.id.text1) as TextView
        val textView2 = row.findViewById<TextView>(R.id.text2) as TextView
        if (item != null) {
            textView1.text = item.getPrimaryText(StyleSpan(Typeface.NORMAL))
            textView2.text = item.getSecondaryText(StyleSpan(Typeface.NORMAL))
        }
        return row
    }

    /**
     * Returns the filter for the current set of autocomplete results.
     */
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()

                // We need a separate list to store the results, since this is run asynchronously.
                var filterData: ArrayList<AutocompletePrediction>? = ArrayList()

                // Skip the autocomplete query if no constraints are given.
                if (constraint != null) {
                    // Query the autocomplete API for the (constraint) search string.
                    filterData = getAutocomplete(constraint)
                }

                results.values = filterData
                results.count = filterData?.size ?: 0
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults?) {

                if (results != null && results.count > 0) {
                    // The API returned at least one result, update the data.
                    mResultList = results.values as ArrayList<AutocompletePrediction>
                    notifyDataSetChanged()
                } else {
                    // The API did not return any results, invalidate the data set.
                    notifyDataSetInvalidated()
                }
            }

            override fun convertResultToString(resultValue: Any): CharSequence {
                // Override this method to display a readable result in the AutocompleteTextView
                // when clicked.
                return if (resultValue is AutocompletePrediction) {
                    resultValue.getFullText(null)
                } else {
                    super.convertResultToString(resultValue)
                }
            }
        }
    }

    /**
     * Submits an autocomplete query to the Places Geo Data Autocomplete API.
     * Results are returned as frozen AutocompletePrediction objects, ready to be cached.
     * objects to store the Place ID and description that the API returns.
     * Returns an empty list if no results were found.
     * Returns null if the API client is not available or the query did not complete
     * successfully.
     * This method MUST be called off the main UI thread, as it will block until data is returned
     * from the API, which may include a network request.
     *
     * @param constraint Autocomplete query string
     * @return Results from the autocomplete API or null if the query was not successful.
     * @see Places.GEO_DATA_API.getAutocomplete
     * @see com.google.android.gms.location.places.AutocompletePrediction.freeze
     */
    private fun getAutocomplete(constraint: CharSequence?): ArrayList<AutocompletePrediction>? {
        if (mGoogleApiClient.isConnected) {

            // Submit the query to the autocomplete API and retrieve a PendingResult that will
            // contain the results when the query completes.
            val results = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, constraint?.toString(),
                    mBounds, mPlaceFilter)

            // This method should have been called off the main UI thread. Block and wait for at most 60s
            // for a result from the API.
            val autocompletePredictions = results.await(60, TimeUnit.SECONDS)

            // Confirm that the query completed successfully, otherwise return null
            val status = autocompletePredictions.status
            if (!status.isSuccess) {
                Toast.makeText(context, "Error contacting API: " + status.toString(), Toast.LENGTH_SHORT).show()
                autocompletePredictions.release()
                return null
            }

            // Freeze the results immutable representation that can be stored safely.
            return DataBufferUtils.freezeAndClose(autocompletePredictions)
        }
        return null
    }

}