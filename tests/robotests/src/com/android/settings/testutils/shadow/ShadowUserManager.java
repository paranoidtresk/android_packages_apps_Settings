/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.settings.testutils.shadow;

import android.annotation.UserIdInt;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.UserManager.EnforcingUser;
import android.util.SparseArray;

import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Implements(value = UserManager.class, inheritImplementationMethods = true)
public class ShadowUserManager extends org.robolectric.shadows.ShadowUserManager {

    private SparseArray<UserInfo> mUserInfos = new SparseArray<>();
    private final List<String> mRestrictions = new ArrayList<>();
    private final Map<String, List<EnforcingUser>> mRestrictionSources = new HashMap<>();
    private final List<UserInfo> mUserProfileInfos = new ArrayList<>();
    private final Set<Integer> mManagedProfiles = new HashSet<>();
    private boolean mIsQuietModeEnabled = false;
    private int[] profileIdsForUser;
    private boolean mUserSwitchEnabled;


    @Resetter
    public void reset() {
        mUserInfos.clear();
        mRestrictions.clear();
        mUserProfileInfos.clear();
        mRestrictionSources.clear();
        mManagedProfiles.clear();
        mIsQuietModeEnabled = false;
        mUserSwitchEnabled = false;
    }

    public void setUserInfo(int userHandle, UserInfo userInfo) {
        mUserInfos.put(userHandle, userInfo);
    }

    @Implementation
    public UserInfo getUserInfo(int userHandle) {
        return mUserInfos.get(userHandle);
    }

    public void addProfile(UserInfo userInfo) {
        mUserProfileInfos.add(userInfo);
    }

    @Implementation
    public List<UserInfo> getProfiles(@UserIdInt int userHandle) {
        return mUserProfileInfos;
    }

    @Implementation
    public int[] getProfileIds(@UserIdInt int userHandle, boolean enabledOnly) {
        int[] ids = new int[mUserProfileInfos.size()];
        for (int i = 0; i < mUserProfileInfos.size(); i++) {
            ids[i] = mUserProfileInfos.get(i).id;
        }
        return ids;
    }

    @Implementation
    public List<UserHandle> getUserProfiles(){
        int[] userIds = getProfileIds(UserHandle.myUserId(), true /* enabledOnly */);
        List<UserHandle> result = new ArrayList<>(userIds.length);
        for (int userId : userIds) {
            result.add(UserHandle.of(userId));
        }
        return result;
    }

    @Implementation
    public int getCredentialOwnerProfile(@UserIdInt int userHandle) {
        return userHandle;
    }

    @Implementation
    public boolean hasBaseUserRestriction(String restrictionKey, UserHandle userHandle) {
        return mRestrictions.contains(restrictionKey);
    }

    public void addBaseUserRestriction(String restriction) {
        mRestrictions.add(restriction);
    }

    public static ShadowUserManager getShadow() {
        return (ShadowUserManager) Shadow.extract(
                RuntimeEnvironment.application.getSystemService(UserManager.class));
    }

    @Implementation
    public List<EnforcingUser> getUserRestrictionSources(
            String restrictionKey, UserHandle userHandle) {
        // Return empty list when there is no enforcing user, otherwise might trigger
        // NullPointer Exception in RestrictedLockUtils.checkIfRestrictionEnforced.
        List<EnforcingUser> enforcingUsers =
                mRestrictionSources.get(restrictionKey + userHandle.getIdentifier());
        return enforcingUsers == null ? Collections.emptyList() : enforcingUsers;
    }

    public void setUserRestrictionSources(
            String restrictionKey, UserHandle userHandle, List<EnforcingUser> enforcers) {
        mRestrictionSources.put(restrictionKey + userHandle.getIdentifier(), enforcers);
    }

    @Implementation
    public boolean isManagedProfile(@UserIdInt int userId) {
        return mManagedProfiles.contains(userId);
    }

    public void addManagedProfile(int userId) {
        mManagedProfiles.add(userId);
    }

    @Implementation
    public boolean isQuietModeEnabled(UserHandle userHandle) {
        return mIsQuietModeEnabled;
    }

    public void setQuietModeEnabled(boolean enabled) {
        mIsQuietModeEnabled = enabled;
    }

    @Implementation
    public int[] getProfileIdsWithDisabled(@UserIdInt int userId) {
        return profileIdsForUser;
    }

    public void setProfileIdsWithDisabled(int[] profileIds) {
        profileIdsForUser = profileIds;
    }

    @Implementation
    public boolean isUserSwitcherEnabled() {
        return mUserSwitchEnabled;
    }

    public void setUserSwitcherEnabled(boolean userSwitchEnabled) {
        mUserSwitchEnabled = userSwitchEnabled;
    }
}
