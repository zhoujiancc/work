/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.bluetooth;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import java.lang.IllegalArgumentException;
import android.util.Log;

import com.mediatek.bluetooth.IBluetoothPrxm;


/**
 * @hide
 */
public class BluetoothPrxm implements BluetoothProfileManager.BluetoothProfileBehavior {

	private static final String TAG = "BtPrxm";

	protected Context mContext;

	protected IBluetoothPrxm mService;

	protected ServiceConnection mConnection = new ServiceConnection(){

		public void onServiceConnected( ComponentName className, IBinder service ){

			mService = IBluetoothPrxm.Stub.asInterface(service);
		}
		public void onServiceDisconnected( ComponentName className ){

			mService = null;
		}
	};

	public BluetoothPrxm( Context context ){

		this.mContext = context;
		if( !context.bindService( new Intent( IBluetoothPrxm.class.getName() ), this.mConnection, Context.BIND_NOT_FOREGROUND ) ){

			Log.e( TAG, "Could not bind to [" + IBluetoothPrxm.class.getName() + "] Service" );
		}
	}

	public synchronized void close(){

		try {
			if( this.mService != null ){

				this.mService = null;
			}
			if( this.mConnection != null ){

				this.mContext.unbindService( this.mConnection );
				this.mConnection = null;
			}
		}
		catch( IllegalArgumentException ex ){

			Log.e( TAG, "Exception occurred in close(): " + ex );
		}
	}

	private boolean isServiceReady(){

		if( this.mService == null ){

			Log.e( TAG, "mService is null");
			return false;
		}
		return true;
	}

	public boolean connect( BluetoothDevice device ){

		if( this.isServiceReady() ){

			try {
				this.mService.connectByProfileManager( device );
				return true;
			}
			catch( RemoteException ex ){

				Log.e( TAG, "Exception occurred in connect(): " + ex );
			}
		}
		return false;
	}

	public boolean disconnect( BluetoothDevice device ){

		if( this.isServiceReady() ){

			try {
				this.mService.disconnect( device.getAddress() );
				return true;
			}
			catch( RemoteException ex ){

				Log.e( TAG, "Exception occurred in disconnect(): " + ex );
			}
		}
		return false;
	}

	public int getState(BluetoothDevice device){

		if( this.isServiceReady() ){

			try {
				return mService.getProfileManagerState( device.getAddress() );
			}
			catch( RemoteException e ){

				Log.e(TAG, "Exception: " + e);
			}
		}
		return BluetoothProfileManager.STATE_UNKNOWN;
	}

	public Set<BluetoothDevice> getConnectedDevices() {

		if( this.isServiceReady() ){

			try {
				return new HashSet<BluetoothDevice>(Arrays.asList(this.mService.getConnectedDevices()));
			}
			catch( RemoteException e ){

				Log.e(TAG, "Exception: " + e);
			}
		}
		return null;
	}
}