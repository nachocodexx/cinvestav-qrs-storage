package com.cinvestav.domain

final case class QRSComplexResult(
                              sensorId:String,
                              measurement:Double,
                              timestamp:Long,
                              filtered_ecg:Double,
                              differentiated_ecg:Double,
                              squared_ecg:Double,
                              integrated_ecg:Double,
                              qrs_timestamp:Long
                            )
