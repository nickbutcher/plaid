# Privacy Policy

If you do not log in to any third party services that Plaid accesses, then it will not store any information about you.

If you do choose to log in to [Designer News](https://www.designernews.co/) or [Dribbble](https://dribbble.com/) then Plaid stores the following information **locally** on your device:

### Dribbble
- **Access Token** identifying your Dribbble account. You can revoke this [here](https://dribbble.com/account/applications) or by logging out.
- Your **name**, **username**, **user id**, **avatar url** & **account type** are stored to be presented in the UI later.

This behavior is implemented [here](https://github.com/nickbutcher/plaid/blob/master/app/src/main/java/io/plaidapp/data/prefs/DribbblePrefs.java) if you'd like to verify it.

### Designer News
- **Access Token** identifying your Designer News account. You can revoke this by logging out.
- Your **username**, **user id** & **avatar url** are stored to be presented in the UI later.

This behavior is implemented [here](https://github.com/nickbutcher/plaid/blob/master/app/src/main/java/io/plaidapp/data/prefs/DesignerNewsPrefs.java).

To ease login to Designer News, Plaid asks to read the accounts stored on your device, this is presented to you as requesting access to your contacts. This is solely used to auto-complete email addresses saved on your device, the app does not in fact read your contacts or send this information off-device. You can verify this behavior [here](https://github.com/nickbutcher/plaid/blob/master/app/src/main/java/io/plaidapp/ui/DesignerNewsLogin.java).
