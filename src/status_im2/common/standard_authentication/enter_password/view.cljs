(ns status-im2.common.standard-authentication.enter-password.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.common.standard-authentication.enter-password.style :as style]
    [status-im2.common.standard-authentication.password-input.view :as password-input]
    [status-im2.contexts.profile.utils :as profile.utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [on-enter-password button-label button-icon-left customization-color]}]
  (let [{:keys [key-uid] :as profile}       (rf/sub [:profile/profile-with-image])
        {:keys [error processing password]} (rf/sub [:profile/login])
        sign-in-enabled?                    (rf/sub [:sign-in-enabled?])]
    [:<>
     [rn/view {:style style/enter-password-container}
      [rn/view
       [quo/text
        {:accessibility-label :sync-code-generated
         :weight              :bold
         :size                :heading-1
         :style               {:margin-bottom 4}}
        (i18n/label :t/enter-password)]
       [rn/view
        {:style style/context-tag}
        [quo/context-tag
         {:type                :default
          :blur?               true
          :profile-picture     (profile.utils/photo profile)
          :full-name           (profile.utils/displayed-name profile)
          :customization-color customization-color
          :size                24}]]
       [password-input/view
        {:processing       processing
         :error            error
         :default-password password
         :sign-in-enabled? sign-in-enabled?}]
       [rn/view {:style style/enter-password-button}
        [quo/button
         {:size                40
          :type                :primary
          :customization-color (or customization-color :primary)
          :accessibility-label :login-button
          :icon-left           button-icon-left
          :disabled?           (or (not sign-in-enabled?) processing)
          :on-press            (fn []
                                 (rf/dispatch [:set-in [:profile/login :key-uid] key-uid])
                                 (rf/dispatch [:profile.login/verify-database-password password
                                               #(on-enter-password password)]))}
         button-label]]]]]))
