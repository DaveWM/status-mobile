(ns status-im2.contexts.chat.messages.view
  (:require
    [react-native.core :as rn]
    [react-native.hooks :as hooks]
    [react-native.reanimated :as reanimated]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.composer.view :as composer.view]
    [status-im2.contexts.chat.messages.contact-requests.bottom-drawer :as contact-requests.bottom-drawer]
    [status-im2.contexts.chat.messages.list.style :as style]
    [status-im2.contexts.chat.messages.list.view :as list.view]
    [status-im2.contexts.chat.messages.navigation.view :as messages.navigation]
    [utils.re-frame :as rf]))

(defn f-chat
  [{:keys [show-floating-scroll-down-button? animate-topbar-name?
           big-name-visible? animate-topbar-opacity? on-end-reached?]
    :as   inner-state-atoms}]
  (let [insets                   (safe-area/get-insets)
        scroll-y                 (reanimated/use-shared-value 0)
        content-height           (reanimated/use-shared-value 0)
        {:keys [keyboard-shown]} (hooks/use-keyboard)
        {:keys [chat-id
                contact-request-state
                group-chat
                able-to-send-message?
                chat-type
                chat-name
                emoji]
         :as   chat}             (rf/sub [:chats/current-chat-chat-view])
        chat-screen-loaded?      (rf/sub [:shell/chat-screen-loaded?])
        all-loaded?              (when chat-screen-loaded?
                                   (rf/sub [:chats/all-loaded? (:chat-id chat)]))
        display-name             (cond
                                   (= chat-type constants/one-to-one-chat-type)
                                   (first (rf/sub
                                           [:contacts/contact-two-names-by-identity
                                            chat-id]))
                                   (= chat-type constants/community-chat-type)
                                   (str (when emoji (str emoji " ")) "# " chat-name)
                                   :else (str emoji chat-name))
        online?                  (rf/sub [:visibility-status-updates/online? chat-id])
        photo-path               (rf/sub [:chats/photo-path chat-id])
        back-icon                (if (= chat-type constants/one-to-one-chat-type)
                                   :i/close
                                   :i/arrow-left)
        {:keys [focused?]}       (rf/sub [:chats/current-chat-input])]
    ;; Note - Don't pass `behavior :height` to keyboard avoiding view,. It breaks composer -
    ;; https://github.com/status-im/status-mobile/issues/16595
    [rn/keyboard-avoiding-view
     {:style                    (style/keyboard-avoiding-container insets)
      :keyboard-vertical-offset (- (:bottom insets))}

     [list.view/message-list-content-view
      {:chat                    chat
       :insets                  insets
       :scroll-y                scroll-y
       :content-height          content-height
       :cover-bg-color          :turquoise
       :keyboard-shown?         keyboard-shown
       :inner-state-atoms       inner-state-atoms
       :animate-topbar-name?    animate-topbar-name?
       :big-name-visible?       big-name-visible?
       :animate-topbar-opacity? animate-topbar-opacity?
       :composer-active?        focused?
       :on-end-reached?         on-end-reached?}]

     [messages.navigation/navigation-view
      {:scroll-y                scroll-y
       :animate-topbar-name?    animate-topbar-name?
       :back-icon               back-icon
       :chat                    chat
       :chat-screen-loaded?     chat-screen-loaded?
       :all-loaded?             all-loaded?
       :display-name            display-name
       :online?                 online?
       :photo-path              photo-path
       :big-name-visible?       big-name-visible?
       :animate-topbar-opacity? animate-topbar-opacity?
       :composer-active?        focused?
       :on-end-reached?         on-end-reached?}]

     (when (seq chat)
       (if able-to-send-message?
         [:f> composer.view/composer
          {:insets                            insets
           :scroll-to-bottom-fn               list.view/scroll-to-bottom
           :show-floating-scroll-down-button? show-floating-scroll-down-button?}]
         [contact-requests.bottom-drawer/view chat-id contact-request-state group-chat]))]))

(defn chat
  []
  (let [inner-state-atoms
        {:extra-keyboard-height             (reagent/atom 0)
         :show-floating-scroll-down-button? (reagent/atom false)
         :messages-view-height              (reagent/atom 0)
         :messages-view-header-height       (reagent/atom 0)
         :animate-topbar-name?              (reagent/atom false)
         :big-name-visible?                 (reagent/atom :initial-render)
         :animate-topbar-opacity?           (reagent/atom false)
         :on-end-reached?                   (reagent/atom false)}]
    [:f> f-chat inner-state-atoms]))
