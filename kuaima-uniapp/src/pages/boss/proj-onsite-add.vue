<template>
  <view class="container">
    <BossPageHeader title="添加驻场人员" />
    <scroll-view scroll-y class="body">
      <!-- 提示 -->
      <view class="perm-tip">
        <text class="perm-ico">ℹ️</text>
        <text class="perm-text">驻场人员将拥有该项目的最高管理权限（考勤、发薪、成员管理），请谨慎授予。</text>
      </view>

      <!-- 角色选择 -->
      <view class="form-card">
        <view class="form-card-title">选择驻场角色</view>
        <view class="role-picker">
          <view
            class="role-option"
            :class="{ selected: role === 'leader' }"
            @click="role = 'leader'"
          >
            <view class="role-radio" />
            <view class="role-info">
              <view class="role-name">项目负责人 <text class="role-badge">最高权限</text></view>
              <view class="role-desc">可管理驻场人员、发薪、考勤、项目配置</view>
            </view>
          </view>
          <view
            class="role-option"
            :class="{ selected: role === 'assistant' }"
            @click="role = 'assistant'"
          >
            <view class="role-radio" />
            <view class="role-info">
              <view class="role-name">项目助理</view>
              <view class="role-desc">协助考勤与发薪，不可管理驻场人员</view>
            </view>
          </view>
        </view>
      </view>

      <!-- 选择人员 -->
      <view class="form-card">
        <view class="form-card-title">选择人员（企业员工）</view>
        <view class="search-people">
          <input class="search-input" v-model="kw" placeholder="输入姓名 / 手机号" @input="render" />
          <view class="search-btn" @click="render"><text class="search-ico">🔍</text></view>
        </view>
        <view class="people-list">
          <view
            v-for="p in filteredPeople"
            :key="p.id"
            class="people-item"
            :class="{ selected: selectedIds.includes(p.id) }"
            @click="toggle(p.id)"
          >
            <view class="check-circle" />
            <view class="pa" :style="{ background: p.color }">{{ p.name.charAt(0) }}</view>
            <view class="pinfo">
              <text class="pname">{{ p.name }}</text>
              <text class="psub">{{ p.phone }} · {{ p.company }}</text>
            </view>
          </view>
          <view v-if="!filteredPeople.length" class="people-empty">未找到匹配员工</view>
        </view>
      </view>
      <view class="bottom-space" />
    </scroll-view>

    <view class="footer">
      <view class="primary-btn" @click="submit">
        <text class="primary-btn-ico">✓</text>确认添加
      </view>
    </view>
  </view>
</template>

<script>
import BossPageHeader from "@/components/BossPageHeader.vue";
import { addOnsite, ONSITE_ROLE_TEXT } from "@/api/project";

const MOCK_PEOPLE = [
  { id: 1, name: "李建国", phone: "138****8899", company: "上海晴时网络科技有限公司", color: "#FF6B35" },
  { id: 2, name: "陈美玲", phone: "139****5521", company: "上海晴时网络科技有限公司", color: "#10B981" },
  { id: 3, name: "王志强", phone: "137****7733", company: "上海晴时网络科技有限公司", color: "#2563EB" },
  { id: 4, name: "刘敏", phone: "136****4418", company: "上海晴时网络科技有限公司", color: "#8B5CF6" },
  { id: 5, name: "赵晓东", phone: "135****2266", company: "上海晴时网络科技有限公司", color: "#F59E0B" },
  { id: 6, name: "孙晓清", phone: "134****9901", company: "上海晴时网络科技有限公司", color: "#EF4444" },
];

export default {
  components: { BossPageHeader },
  data() {
    return {
      projectId: "",
      role: "leader",
      kw: "",
      people: MOCK_PEOPLE,
      selectedIds: [],
    };
  },
  computed: {
    filteredPeople() {
      const kw = this.kw.trim();
      if (!kw) return this.people;
      return this.people.filter(
        (p) => p.name.includes(kw) || p.phone.includes(kw),
      );
    },
  },
  onLoad(query) {
    this.projectId = query.id;
  },
  methods: {
    ONSITE_ROLE_TEXT,
    render() {},
    toggle(id) {
      const idx = this.selectedIds.indexOf(id);
      if (idx >= 0) this.selectedIds.splice(idx, 1);
      else this.selectedIds.push(id);
    },
    async submit() {
      if (!this.selectedIds.length) {
        uni.showToast({ title: "请至少选择 1 名员工", icon: "none" });
        return;
      }
      const picked = this.people.filter((p) => this.selectedIds.includes(p.id));
      try {
        await Promise.all(
          picked.map((p) =>
            addOnsite(this.projectId, {
              name: p.name,
              phone: p.phone,
              company: p.company,
              onsiteRole: this.role,
              onsiteDays: 0,
            }),
          ),
        );
        const roleLabel = ONSITE_ROLE_TEXT[this.role];
        uni.showToast({ title: `已添加 ${picked.length} 名${roleLabel}`, icon: "success" });
        setTimeout(() => uni.navigateBack({ fail: () => uni.reLaunch({ url: "/pages/boss/proj-onsite?id=" + this.projectId }) }), 800);
      } catch (e) {
        uni.showToast({ title: "添加失败，请重试", icon: "none" });
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.container { display: flex; flex-direction: column; height: 100vh; background: #f3f4f6; }
.body { flex: 1; overflow-y: auto; padding: 12px 16px 0; }
.perm-tip {
  font-size: 12px;
  color: #ff6b35;
  background: #fff7f0;
  padding: 10px 12px;
  border-radius: 10px;
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.perm-ico { flex-shrink: 0; }
.perm-text { line-height: 1.5; }
.form-card {
  background: #fff;
  border-radius: 14px;
  padding: 14px 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}
.form-card-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.form-card-title::before { content: ""; width: 3px; height: 12px; background: #ff6b35; border-radius: 2px; }
.role-picker { display: flex; flex-direction: column; gap: 10px; }
.role-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid #eee;
  background: #fafafa;
}
.role-option.selected { border-color: #ff6b35; background: #fff7f0; }
.role-radio {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid #ddd;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.role-option.selected .role-radio { border-color: #ff6b35; background: #ff6b35; }
.role-option.selected .role-radio::after { content: ""; width: 6px; height: 6px; border-radius: 50%; background: #fff; }
.role-info { flex: 1; min-width: 0; }
.role-name { font-size: 14px; font-weight: 600; color: #333; }
.role-desc { font-size: 11px; color: #999; margin-top: 3px; }
.role-badge { font-size: 11px; padding: 2px 6px; border-radius: 4px; background: #fff0e8; color: #ff6b35; margin-left: 4px; }
.search-people { display: flex; gap: 10px; margin-bottom: 12px; }
.search-input {
  flex: 1;
  border: 1px solid #eee;
  border-radius: 10px;
  padding: 10px 14px;
  font-size: 13px;
}
.search-btn {
  background: #ff6b35;
  color: #fff;
  border-radius: 10px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}
.people-list { display: flex; flex-direction: column; gap: 8px; }
.people-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #f0f0f0;
}
.people-item.selected { border-color: #ff6b35; background: #fff7f0; }
.check-circle { width: 18px; height: 18px; border-radius: 50%; border: 2px solid #ddd; flex-shrink: 0; }
.people-item.selected .check-circle { border-color: #ff6b35; background: #ff6b35; position: relative; }
.people-item.selected .check-circle::after { content: "✓"; color: #fff; font-size: 11px; position: absolute; top: -2px; left: 3px; }
.pa {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
}
.pinfo { flex: 1; min-width: 0; }
.pname { font-size: 14px; color: #333; font-weight: 500; display: block; }
.psub { font-size: 11px; color: #999; margin-top: 2px; display: block; }
.people-empty { text-align: center; padding: 20px 0; color: #bbb; font-size: 13px; }
.bottom-space { height: 16px; }
.footer {
  flex-shrink: 0;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: #fff;
  border-top: 0.5px solid #f0f0f0;
}
.primary-btn {
  background: linear-gradient(135deg, #ff6b35, #ff8c5a);
  color: #fff;
  border-radius: 24px;
  text-align: center;
  padding: 13px 0;
  font-size: 15px;
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(255, 107, 53, 0.3);
}
.primary-btn-ico { margin-right: 4px; }
</style>
