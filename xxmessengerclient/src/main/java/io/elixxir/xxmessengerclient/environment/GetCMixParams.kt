package io.elixxir.xxmessengerclient.environment

import io.elixxir.xxclient.bindings.Bindings
import io.elixxir.xxclient.utils.Data
import io.elixxir.xxmessengerclient.utils.nonNullResultOf

class GetCMixParams(private val bindings: Bindings) {

    operator fun invoke(): Result<Data> {
        return nonNullResultOf { bindings.defaultCmixParams }
    }

    /*
    extension GetCMixParams {
  public static let liveDefault = GetCMixParams {
    guard let data = BindingsGetDefaultCMixParams() else {
      fatalError("BindingsGetDefaultCMixParams returned `nil`")
    }
    return data
  }
}
     */
}