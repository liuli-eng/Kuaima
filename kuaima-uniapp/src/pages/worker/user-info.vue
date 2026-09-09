<template>
  <view class="page">
    <AppNavBar title="个人信息" :show-back="true" />
    <scroll-view scroll-y class="content">
      <view v-if="loading" class="page-state">个人信息加载中…</view>
      <view v-else-if="loadError" class="page-state error" @click="loadProfile">加载失败，点击重试</view>
      <template v-else>
        <view class="avatar-section" @click="editAvatar">
          <image v-if="form.avatar" class="avatar-image" :src="form.avatar" mode="aspectFill" />
          <view v-else class="avatar">👤</view>
          <text class="avatar-tip">点击填写头像图片地址</text>
        </view>
        <view class="card">
          <view class="row"><text class="label">昵称</text><input v-model="form.nickname" class="value" placeholder="请输入昵称" /></view>
          <view class="row readonly"><text class="label">手机号</text><text class="value text-value">{{ phone || "未绑定" }}</text></view>
          <picker :range="genderOptions" :value="genderIndex" @change="changeGender"><view class="row"><text class="label">性别</text><text class="value text-value" :class="{ placeholder: !form.gender }">{{ form.gender || "请选择" }}</text><text class="arrow">›</text></view></picker>
          <picker mode="date" :value="form.birthday" start="1940-01-01" :end="today" @change="changeBirthday"><view class="row"><text class="label">生日</text><text class="value text-value" :class="{ placeholder: !form.birthday }">{{ form.birthday || "请选择" }}</text><text class="arrow">›</text></view></picker>
          <view class="row"><text class="label">居住地</text><input v-model="form.city" class="value" placeholder="请输入所在城市" /></view>
        </view>
        <view class="card">
          <view class="row"><text class="label">擅长工种</text><input v-model="form.skills" class="value" placeholder="多个工种用逗号分隔" /></view>
          <view class="row"><text class="label">工作年限</text><input v-model="form.workYears" class="value" type="number" placeholder="请输入年限" /><text class="suffix">年</text></view>
          <view class="row"><text class="label">接受夜班</text><switch :checked="form.acceptNightShift" color="#ff6b35" @change="changeNightShift" /></view>
          <view class="intro-row"><text class="label">个人简介</text><textarea v-model="form.introduction" class="intro" maxlength="500" placeholder="介绍一下自己的工作经验" /><text class="count">{{ form.introduction.length }}/500</text></view>
        </view>
        <view class="scroll-space"></view>
      </template>
    </scroll-view>
    <view v-if="!loading && !loadError" class="bottom-bar"><button class="save-button" :disabled="saving" @click="saveProfile">{{ saving ? "保存中…" : "保存资料" }}</button></view>
  </view>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import AppNavBar from "@/components/AppNavBar.vue";
import { getWorkerProfile, updateWorkerProfile } from "@/api/backend";

const genderOptions = ["男", "女", "保密"];
const loading = ref(false), loadError = ref(false), saving = ref(false), phone = ref("");
const today = formatDate(new Date());
const form = reactive({ avatar: "", nickname: "", gender: "", birthday: "", city: "", skills: "", workYears: "", acceptNightShift: false, introduction: "" });
const genderIndex = computed(() => Math.max(0, genderOptions.indexOf(form.gender)));
onMounted(loadProfile);

async function loadProfile() {
  loading.value = true; loadError.value = false;
  try {
    const profile = await getWorkerProfile();
    form.avatar = profile?.avatar || "";
    form.nickname = profile?.nickname || profile?.name || "";
    form.gender = profile?.gender || "";
    form.birthday = normalizeDate(profile?.birthday);
    form.city = profile?.city || "";
    form.skills = profile?.skills || "";
    form.workYears = profile?.workYears === undefined || profile?.workYears === null ? "" : String(profile.workYears);
    form.acceptNightShift = profile?.acceptNightShift === true;
    form.introduction = profile?.introduction || "";
    phone.value = profile?.phone || profile?.phoneNumber || "";
  } catch (error) { loadError.value = true; uni.showToast({ title: error.message || "个人信息加载失败", icon: "none" }); }
  finally { loading.value = false; }
}
function changeGender(event) { form.gender = genderOptions[Number(event.detail.value)] || ""; }
function changeBirthday(event) { form.birthday = event.detail.value || ""; }
function changeNightShift(event) { form.acceptNightShift = event.detail.value === true; }
function editAvatar() {
  uni.showModal({ title: "头像图片地址", editable: true, placeholderText: "请输入 https 图片地址", content: form.avatar, success: ({ confirm, content }) => {
    if (!confirm) return;
    const avatar = String(content || "").trim();
    if (avatar && !/^https?:\/\//i.test(avatar)) return uni.showToast({ title: "请输入有效的图片地址", icon: "none" });
    form.avatar = avatar;
  } });
}
async function saveProfile() {
  if (saving.value) return;
  const nickname = form.nickname.trim();
  if (!nickname) return uni.showToast({ title: "请输入昵称", icon: "none" });
  const workYears = form.workYears === "" ? null : Number(form.workYears);
  if (workYears !== null && (!Number.isInteger(workYears) || workYears < 0 || workYears > 80)) return uni.showToast({ title: "请输入正确的工作年限", icon: "none" });
  saving.value = true;
  try {
    await updateWorkerProfile({ avatar: form.avatar.trim(), nickname, gender: form.gender, birthday: form.birthday || null, city: form.city.trim(), skills: form.skills.trim(), workYears, acceptNightShift: form.acceptNightShift, introduction: form.introduction.trim() });
    const cached = uni.getStorageSync("userInfo") || {};
    uni.setStorageSync("userInfo", { ...cached, avatar: form.avatar.trim(), nickname });
    uni.showToast({ title: "资料已保存", icon: "success" }); await loadProfile();
  } catch (error) { uni.showToast({ title: error.message || "保存失败", icon: "none" }); }
  finally { saving.value = false; }
}
function normalizeDate(value) { return value ? String(value).slice(0, 10) : ""; }
function formatDate(date) { return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`; }
</script>

<style scoped>
.page { height: 100vh; display: flex; flex-direction: column; overflow: hidden; background: #f5f5f5; }
.content { flex: 1; height: 0; min-height: 0; }
.page-state { padding: 220rpx 32rpx; text-align: center; color: #999; font-size: 26rpx; }
.page-state.error { color: #ff6b35; }
.avatar-section { padding: 42rpx 32rpx 32rpx; text-align: center; background: #fff; }
.avatar, .avatar-image { width: 144rpx; height: 144rpx; margin: 0 auto 16rpx; border-radius: 50%; }
.avatar { display: flex; align-items: center; justify-content: center; background: #ffe4b5; color: #ff6b35; font-size: 70rpx; }
.avatar-tip { color: #999; font-size: 23rpx; }
.card { margin: 24rpx 32rpx 0; overflow: hidden; border-radius: 24rpx; background: #fff; }
.row { display: flex; align-items: center; min-height: 100rpx; padding: 0 28rpx; border-bottom: 1rpx solid #f2f2f2; box-sizing: border-box; }
.row:last-child { border-bottom: 0; }
.row.readonly { background: #fcfcfc; }
.label { flex-shrink: 0; width: 190rpx; color: #333; font-size: 28rpx; }
.value { flex: 1; min-width: 0; color: #555; font-size: 27rpx; text-align: right; }
.text-value { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.placeholder { color: #bbb; }
.arrow { margin-left: 14rpx; color: #ccc; font-size: 34rpx; }
.suffix { margin-left: 8rpx; color: #777; font-size: 26rpx; }
.intro-row { position: relative; padding: 28rpx; }
.intro-row .label { display: block; width: auto; margin-bottom: 18rpx; }
.intro { width: 100%; height: 180rpx; padding: 20rpx; border-radius: 16rpx; background: #f7f7f7; color: #555; font-size: 26rpx; box-sizing: border-box; }
.count { position: absolute; right: 46rpx; bottom: 42rpx; color: #bbb; font-size: 21rpx; }
.scroll-space { height: 32rpx; }
.bottom-bar { flex-shrink: 0; padding: 18rpx 32rpx calc(18rpx + env(safe-area-inset-bottom)); border-top: 1rpx solid #eee; background: #fff; }
.save-button { height: 86rpx; margin: 0; border: 0; border-radius: 44rpx; background: linear-gradient(135deg, #ff6b35, #ff8c5a); color: #fff; font-size: 29rpx; font-weight: 600; line-height: 86rpx; }
.save-button::after { border: 0; }
.save-button[disabled] { opacity: .55; }
</style>
