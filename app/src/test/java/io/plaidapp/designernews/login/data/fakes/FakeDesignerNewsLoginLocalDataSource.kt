package io.plaidapp.designernews.login.data.fakes

import io.plaidapp.designernews.login.data.DesignerNewsLoginLocalDataSource

class FakeDesignerNewsLoginLocalDataSource() : DesignerNewsLoginLocalDataSource() {
    override fun clearData() {
        // nothing to do here
    }
}