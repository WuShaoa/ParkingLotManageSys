package com.ipark.model

import org.springframework.stereotype.Component
import java.sql.Timestamp

@Component
data class EntryRecord(
    var car_id:String? = null,
    var car_type:Int? = null,
    var entrance_time: Timestamp? = null,
    var exit_time: Timestamp? = null,
    var fee_standard: Float? = null,
    //var record_id: Int? = null

)
