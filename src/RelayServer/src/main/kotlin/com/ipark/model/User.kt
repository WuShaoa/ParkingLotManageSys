package com.ipark.model

import org.springframework.stereotype.Component

@Component
data class User(

    var user_id :Int? = null,
    var user_account :String? = null,
    var user_password: String? = null,
    var user_role: Int? = null

)
