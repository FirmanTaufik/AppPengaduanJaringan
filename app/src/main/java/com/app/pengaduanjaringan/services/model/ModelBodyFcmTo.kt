package com.app.pengaduanjaringan.services.model

import com.app.pengaduanjaringan.services.model.ModelNotification

class ModelBodyFcmTo {
    private var to: String? = null
    private var notification: ModelNotification? = null

    constructor() {}
    constructor(to: String?, notification: ModelNotification?) {
        this.to = to
        this.notification = notification
    }
}