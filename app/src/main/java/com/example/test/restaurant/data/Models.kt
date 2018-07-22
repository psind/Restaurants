package com.example.test.restaurant.data

/**
 * @author Prateek on 20/07/18.
 */
object Models {

    data class PlacesFromTextResponse(val results: ArrayList<RestaurantsInfoModel>?,
                                      val status: String?)

    data class RestaurantsInfoModel(val icon: String?,
                                    val id: String?,
                                    val name: String?,
                                    val rating: String?,
                                    val photos: ArrayList<PhotosModel>?,
                                    val vicinity: String?,
                                    val types: ArrayList<String>?,
                                    val isSelected: Boolean = false)

    data class PhotosModel(val html_attributions: ArrayList<String>?,
                           val height: String?,
                           val width: String?)

    data class SelectType(val type: String,
                          var isSelected: Boolean)

    data class RestaurantData(val string: String)
}