package com.ipark.model

import org.springframework.stereotype.Component
import java.sql.Timestamp

@Component
data class MemberInfo(
    var member_id: Int? = null,
    var car_id: String? = null,
    var car_type: Int? = 0,
    var account_end_time: Timestamp? = null,
    //var use_id: Int? = null


)
