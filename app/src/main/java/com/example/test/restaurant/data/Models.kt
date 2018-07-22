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

/*{
    "html_attributions": [],
    "results": [
        {
            "geometry": {
                "location": {
                    "lat": -33.8585858,
                    "lng": 151.2100415
                },
                "viewport": {
                    "northeast": {
                        "lat": -33.85723597010728,
                        "lng": 151.2113913298927
                    },
                    "southwest": {
                        "lat": -33.85993562989272,
                        "lng": 151.2086916701072
                    }
                }
            },
            "icon": "https://maps.gstatic.com/mapfiles/place_api/icons/bar-71.png",
            "id": "8e980ad0c819c33cdb1cea31e72d654ca61a7065",
            "name": "Cruise Bar, Restaurant & Events",
            "opening_hours": {
                "open_now": false
            },
            "photos": [
                {
                    "height": 1134,
                    "html_attributions": [
                        "<a href=\"https://maps.google.com/maps/contrib/112582655193348962755/photos\">Cruise Bar, Restaurant &amp; Events</a>"
                    ],
                    "photo_reference": "CmRaAAAAyr4y08LeSxjpVU1ItVXbeD0NYlxPPkS3UADmy4p1tcwPVCTNA4UODTSnTW6Dn632WO-wXSURraB5DHIGdwLTVlORQfiFmIJ-OGqqgpY-WRYZ9V8wZdwp4rkmuttBu8jaEhBh1F3HT3WskfNgMBQdwJpbGhQjQv-h43q4aHCZOiIgr4ILEIRv7w",
                    "width": 2048
                }
            ],
            "place_id": "ChIJi6C1MxquEmsR9-c-3O48ykI",
            "plus_code": {
                "compound_code": "46R6+H2 The Rocks, New South Wales",
                "global_code": "4RRH46R6+H2"
            },
            "rating": 4,
            "reference": "CmRbAAAArrN4g75-WZgRk2cX_nNVpzz9wwaAxn_iThLo2w_FDGcC_7fPFp-uOso0C9XHW4ApkC5ZZmZ0A7s6jkg8lRIlXHDxpnk8oSbo97jSToj7MZSNDgaziToDfkjg9AoTMXoUEhBxP-rPTZ6pPasiLkrowKbXGhRkjlhAHkt4RttFvyzy8UPlMeJqIg",
            "scope": "GOOGLE",
            "types": [
                "bar",
                "restaurant",
                "food",
                "point_of_interest",
                "establishment"
            ],
            "vicinity": "Circular Quay W, Sydney"
        },/*/