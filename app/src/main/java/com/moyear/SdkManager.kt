package com.moyear

class SdkManager {

    private constructor()

    companion object {

        private var INSTANCE: SdkManager? = null

        @JvmStatic
        fun getInstance(): SdkManager {
            if (INSTANCE == null)
                INSTANCE = SdkManager()

            return INSTANCE!!
        }

    }

}