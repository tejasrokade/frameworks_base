/*
 * Copyright (C) 2018 Benzo Rom
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.service.quicksettings.Tile;

import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import com.android.internal.util.xtended.OnTheGoActions;
import com.android.internal.util.xtended.OnTheGoUtils;
import com.android.internal.util.xtended.XtendedUtils;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;

/** Quick settings tile: OnTheGo Mode **/
public class OnTheGoTile extends QSTileImpl<BooleanState> {

    private static final String SERVICE_NAME = "com.android.systemui.xtended.onthego.OnTheGoService";

    private final Icon mIcon = ResourceIcon.get(R.drawable.ic_qs_onthego);

    public OnTheGoTile(QSHost host) {
        super(host);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
    }

    protected void toggleState() {
        Intent service = (new Intent())
                .setClassName("com.android.systemui",
                "com.android.systemui.xtended.onthego.OnTheGoService");
        OnTheGoActions.processAction(mContext,
                OnTheGoActions.ACTION_ONTHEGO_TOGGLE);
    }

    @Override
    protected void handleClick() {
        mHost.collapsePanels();
        //finish collapsing the panel
        try {
             Thread.sleep(1000); //1s
        } catch (InterruptedException ie) {}
        toggleState();
        refreshState();
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_onthego_label);
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    protected void handleLongClick() {
        handleClick();
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        state.icon = mIcon;
        state.contentDescription =  mContext.getString(
                R.string.quick_settings_onthego_label);
        state.label = mContext.getString(R.string.quick_settings_onthego_label);
        state.value = XtendedUtils.isServiceRunning(mContext, SERVICE_NAME);
        state.state = state.value ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.XTENDED;
    }

    @Override
    protected String composeChangeAnnouncement() {
        return mContext.getString(R.string.quick_settings_onthego_label);
    }
}

