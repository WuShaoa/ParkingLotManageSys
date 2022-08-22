package com.ipark.model

import org.springframework.stereotype.Component
import java.sql.Timestamp

@Component
data class BlackList(
    var black_list_id :Int? = null,
    var car_id: String? = null,
    var violation_time: Timestamp? =null,
    var violation_reason: String? =null,


)
