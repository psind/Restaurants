package com.example.test.restaurant.ui

import android.graphics.LightingColorFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.test.restaurant.*
import com.example.test.restaurant.data.Models
import com.example.test.restaurant.data.RetrofitService
import com.example.test.restaurant.data.SnackBarListener
import com.example.test.restaurant.data.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_search_tab.*
import kotlinx.android.synthetic.main.restaurant_list_item.view.*

/**
 * @author Prateek on 17/07/18.
 */
class SearchTabFragment : Fragment() {

    private val retrofitService by lazy { RetrofitService.create() }
    private var disposable: Disposable? = null

    var searchText = ""
    var adapter: RestaurantsAdapter? = null
    var restauratnsList: ArrayList<Models.RestaurantsInfoModel> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_search_tab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar?.indeterminateDrawable?.colorFilter = LightingColorFilter(0x1000000,
                ContextCompat.getColor(activity!!, R.color.colorAccent))

        clearSearchIV?.setOnClickListener {
            searchRestaurantET?.setText("")
        }
        searchRestaurantET?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0?.length ?: 0 > 2) {
                    searchText = p0.toString()
                    getRestaurantsList()
                } else if(p0?.length ?: 0 ==0){
                    searchText = getString(R.string.app_name)
                    getRestaurantsList()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

        })

        restaurantsRV?.layoutManager = LinearLayoutManager(context)
        adapter = RestaurantsAdapter()
        restaurantsRV?.adapter = adapter
        searchText = getString(R.string.app_name)
        getRestaurantsList()
    }

    private fun getRestaurantsList() {
        progressBar?.visibility = View.VISIBLE
        if (Utils.checkInternet(context)) {
            disposable = retrofitService.nearbySearchWithText((activity as RestaurantActivity).getLongAndLat()
                    , searchText, Utils.GOOGLE_PLACES_KEY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                progressBar?.visibility = View.GONE
                                if (result.results?.isNotEmpty()!!) {
                                    restaurantsRV?.visibility = VISIBLE
                                    noRestaurantTV?.visibility = GONE
                                    restauratnsList = result.results
                                    adapter?.notifyDataSetChanged()
                                } else {
                                    restaurantsRV?.visibility = GONE
                                    noRestaurantTV?.visibility = VISIBLE
                                }
                            },
                            { error ->
                                progressBar?.visibility = View.GONE
                            }
                    )
        } else {
            progressBar?.visibility = View.GONE
            Utils.showSnackBar(context, object : SnackBarListener {
                override fun onRetryClickedFromSnackBar() {
                    getRestaurantsList()
                }

            }, getString(R.string.snackBar_internet_connection), exploreTabLayout, true)
        }
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onResume() {
        super.onResume()
        locationTV?.text = "Address : ${(activity as RestaurantActivity).getAddress()}"
    }

    inner class RestaurantsAdapter : RecyclerView.Adapter<Holder>() {

        private var currentPos = -1

        override fun onBindViewHolder(holder: Holder, position: Int) {

            val item = restauratnsList[position]
            if (!TextUtils.isEmpty(item.name))
                holder.view.nameTV?.text = item.name

            if (!TextUtils.isEmpty(item.icon) && activity != null
                    && holder.view.restaurantThumbIV != null) {
                Glide.with(activity!!).load(item.icon)
                        .into(holder.view.restaurantThumbIV)
            } else if(activity != null){
                holder.view.restaurantThumbIV?.background = ContextCompat.getDrawable(activity!!, R.drawable.ic_launcher_background)
            }

            if (currentPos == position) {
                holder.view.expandableLayout?.visibility = View.VISIBLE
                if (!TextUtils.isEmpty(item.rating))
                    holder.view.ratingTV?.text = item.rating
                else {
                    holder.view.ratingTV?.visibility = GONE
                    holder.view.ratingText?.visibility = GONE
                }

                if (!TextUtils.isEmpty(item.vicinity))
                    holder.view.addressTV?.text = item.vicinity
                else {
                    holder.view.addressTV?.visibility = GONE
                    holder.view.addressText?.visibility = GONE
                }
            } else {
                holder.view.expandableLayout?.visibility = View.GONE

            }

            holder.view.setOnClickListener {
                currentPos = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int = restauratnsList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.restaurant_list_item, parent, false))

    }

}