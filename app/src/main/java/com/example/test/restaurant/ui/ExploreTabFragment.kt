package com.example.test.restaurant.ui

import android.graphics.LightingColorFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.util.Log
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
import kotlinx.android.synthetic.main.fragment_explore_tab.*
import kotlinx.android.synthetic.main.restaurant_list_item.view.*
import kotlinx.android.synthetic.main.restaurant_type_list_item.view.*

/**
 * @author Prateek on 17/07/18.
 */
class ExploreTabFragment : Fragment() {

    private val retrofitService by lazy { RetrofitService.create() }
    private var disposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_explore_tab, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar?.indeterminateDrawable?.colorFilter = LightingColorFilter(0x1000000,
                ContextCompat.getColor(activity!!, R.color.colorAccent))

    }

    override fun onStart() {
        super.onStart()
        selectTypeRV?.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        restaurantsRV?.layoutManager = LinearLayoutManager(context)
        val selectTypeList = ArrayList<Models.SelectType>()
        resources.getStringArray(R.array.restaurant_type_list).forEachIndexed { index, s ->
            selectTypeList.add(Models.SelectType(s, index == 0))
        }
        selectTypeRV?.adapter = SelectTypeAdapter(selectTypeList)
    }

    private fun getRestaurantsList(selectedType: String) {
        progressBar?.visibility = VISIBLE
        if (Utils.checkInternet(context)) {
            disposable = retrofitService.nearbySearch((activity as RestaurantActivity).getLongAndLat()
                    , selectedType.toLowerCase(), Utils.GOOGLE_PLACES_KEY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { result ->
                                progressBar?.visibility = View.GONE
                                if (result.results?.isNotEmpty()!!) {
                                    restaurantsRV?.visibility = VISIBLE
                                    noRestaurantTV?.visibility = GONE
                                    restaurantsRV?.adapter = RestaurantsAdapter(result.results)
                                } else {
                                    restaurantsRV?.visibility = GONE
                                    noRestaurantTV?.visibility = VISIBLE
                                }
                                Log.i("Places Response", result.toString())
                            },
                            { error ->
                                progressBar?.visibility = GONE
                                Log.i("Places Error", error.message)
                            }
                    )
        } else {
            progressBar?.visibility = GONE
            Utils.showSnackBar(context, object : SnackBarListener {
                override fun onRetryClickedFromSnackBar() {
                    getRestaurantsList(selectedType)
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

    inner class SelectTypeAdapter(private val selectTypeList: ArrayList<Models.SelectType>) : RecyclerView.Adapter<Holder>() {

        private var currentPos = 0

        override fun onBindViewHolder(holder: Holder, position: Int) {

            val item = selectTypeList[position]

            holder.view.typeTV?.text = item.type.replace("_", " ")

            if (position == currentPos) {
                holder.view.typeTV?.background = ContextCompat.getDrawable(context!!, R.drawable.rounded_corner_orange)
                holder.view.typeTV?.setTextColor(ContextCompat.getColor(context!!, R.color.white))
                getRestaurantsList(item.type)
            } else {
                holder.view.typeTV?.setTextColor(ContextCompat.getColor(context!!, R.color.grey_28))
                holder.view.typeTV?.background = ContextCompat.getDrawable(context!!, R.drawable.rounded_corner_white)
            }

            holder.view.setOnClickListener {
                currentPos = position
                notifyDataSetChanged()
            }
        }

        override fun getItemCount(): Int = selectTypeList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.restaurant_type_list_item, parent, false))

    }

    inner class RestaurantsAdapter(private val restaurantsList: ArrayList<Models.RestaurantsInfoModel>) : RecyclerView.Adapter<Holder>() {

        private var currentPos = -1

        override fun onBindViewHolder(holder: Holder, position: Int) {

            val item = restaurantsList[position]
            if (!TextUtils.isEmpty(item.name))
                holder.view.nameTV?.text = item.name

            if (!TextUtils.isEmpty(item.icon) && activity != null
                    && holder.view.restaurantThumbIV != null) {
                Glide.with(activity!!).load(item.icon)
                        .into(holder.view.restaurantThumbIV)
            } else {
                holder.view.restaurantThumbIV.background = ContextCompat.getDrawable(activity!!, R.drawable.ic_launcher_background)
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

        override fun getItemCount(): Int = restaurantsList.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
                Holder(LayoutInflater.from(parent.context).inflate(R.layout.restaurant_list_item, parent, false))

    }

}

class Holder(val view: View) : RecyclerView.ViewHolder(view)


