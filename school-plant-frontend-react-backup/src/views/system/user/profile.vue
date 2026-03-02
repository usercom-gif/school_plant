<template>
  <div class="profile-container">
    <a-row :gutter="24">
      <!-- 个人信息卡片 -->
      <a-col :span="8">
        <a-card :bordered="false" class="user-info-card">
          <div class="user-profile-header">
            <div class="avatar-wrapper" @click="handleAvatarClick">
              <a-avatar
                :size="100"
                :src="userInfo.avatarUrl || defaultAvatar"
                class="user-avatar"
              />
              <div class="avatar-mask"><EditOutlined /> 修改头像</div>
            </div>
            <div class="user-name">{{ userInfo.realName }}</div>
          </div>
          <a-divider />
          <div class="user-profile-detail">
            <p><UserOutlined /> 用户名：{{ userInfo.username }}</p>
            <p v-if="userInfo.gradeName">
              <BankOutlined /> 年级：{{ userInfo.gradeName }}
            </p>
            <p v-if="userInfo.className">
              <TeamOutlined /> 班级：{{ userInfo.className }}
            </p>
            <p><PhoneOutlined /> 手机号：{{ userInfo.phone || "未绑定" }}</p>
            <p><MailOutlined /> 邮箱：{{ userInfo.email || "未绑定" }}</p>
            <p>
              <CalendarOutlined /> 注册时间：{{
                formatDate(userInfo.createdAt)
              }}
            </p>
          </div>
        </a-card>
      </a-col>

      <!-- 编辑资料与修改密码 -->
      <a-col :span="16">
        <a-card :bordered="false" class="tabs-card">
          <a-tabs v-model:activeKey="activeTab">
            <a-tab-pane key="1" tab="基本资料">
              <a-form
                :model="profileForm"
                :label-col="{ span: 4 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="真实姓名">
                  <div style="display: flex; gap: 8px">
                    <a-input v-model:value="profileForm.realName" disabled />
                    <a-button type="primary" @click="nameChangeVisible = true"
                      >申请更名</a-button
                    >
                  </div>
                </a-form-item>
                <a-form-item label="手机号码">
                  <a-input v-model:value="profileForm.phone" />
                </a-form-item>
                <a-form-item label="邮箱">
                  <a-input v-model:value="profileForm.email" />
                </a-form-item>
                <a-form-item :wrapper-col="{ offset: 4 }">
                  <a-button
                    type="primary"
                    @click="handleUpdateProfile"
                    :loading="profileLoading"
                    >保存修改</a-button
                  >
                </a-form-item>
              </a-form>
            </a-tab-pane>

            <a-tab-pane key="2" tab="修改密码">
              <a-alert
                message="为了账户安全，密码必须包含大小写字母、数字和特殊字符，且长度至少8位"
                type="info"
                show-icon
                style="margin-bottom: 20px"
              />
              <a-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                :label-col="{ span: 4 }"
                :wrapper-col="{ span: 16 }"
              >
                <a-form-item label="原密码" name="oldPassword">
                  <a-input-password
                    v-model:value="pwdForm.oldPassword"
                    placeholder="请输入原密码"
                  />
                </a-form-item>
                <a-form-item label="新密码" name="newPassword">
                  <a-input-password
                    v-model:value="pwdForm.newPassword"
                    placeholder="请输入新密码"
                  />
                </a-form-item>
                <a-form-item label="确认密码" name="confirmPassword">
                  <a-input-password
                    v-model:value="pwdForm.confirmPassword"
                    placeholder="请再次输入新密码"
                  />
                </a-form-item>
                <a-form-item :wrapper-col="{ offset: 4 }">
                  <a-button
                    type="primary"
                    danger
                    @click="handleUpdatePwd"
                    :loading="pwdLoading"
                    >确认修改</a-button
                  >
                </a-form-item>
              </a-form>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>
    </a-row>

    <!-- 头像修改模态框 -->
    <a-modal
      v-model:visible="avatarModalVisible"
      title="修改头像"
      :footer="null"
      :width="400"
    >
      <div class="avatar-modal-content">
        <div class="avatar-preview">
          <a-image :width="200" :src="userInfo.avatarUrl || defaultAvatar" />
        </div>
        <div class="avatar-upload-btn">
          <a-upload
            name="file"
            :show-upload-list="false"
            :customRequest="handleUploadAvatar"
            :before-upload="beforeUpload"
            accept="image/*"
          >
            <a-button type="primary"> <UploadOutlined /> 选择新头像 </a-button>
          </a-upload>
          <div class="upload-tip">支持 jpg/png 格式，小于 2MB</div>
        </div>
      </div>
    </a-modal>

    <!-- Name Change Modal -->
    <a-modal
      v-model:visible="nameChangeVisible"
      title="申请更名"
      @ok="handleNameChange"
    >
      <a-form :label-col="{ span: 6 }">
        <a-form-item label="当前姓名">
          <a-input :value="profileForm.realName" disabled />
        </a-form-item>
        <a-form-item label="新姓名" required>
          <a-input
            v-model:value="nameChangeForm.newName"
            placeholder="请输入新姓名"
          />
        </a-form-item>
        <a-form-item label="申请理由" required>
          <a-textarea
            v-model:value="nameChangeForm.reason"
            placeholder="请输入申请理由"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from "vue";
import {
  UserOutlined,
  PhoneOutlined,
  MailOutlined,
  CalendarOutlined,
  EditOutlined,
  UploadOutlined,
  TeamOutlined,
  BankOutlined,
} from "@ant-design/icons-vue";
import { message, Modal } from "ant-design-vue";
import {
  getUserInfo,
  updateProfile,
  updatePassword,
  submitNameChange,
} from "@/api/user";
import { uploadFile } from "@/api/common";
import { useUserStore } from "@/store/user";
import { useRouter } from "vue-router";
import dayjs from "dayjs";

const router = useRouter();
const userStore = useUserStore();
const defaultAvatar =
  "https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png";

const userInfo = ref<any>({});
const userRole = ref("");
const activeTab = ref("1");
const avatarModalVisible = ref(false);
const nameChangeVisible = ref(false);

const profileLoading = ref(false);
const profileForm = reactive({
  realName: "",
  phone: "",
  email: "",
  avatarUrl: "",
});

const nameChangeForm = reactive({
  newName: "",
  reason: "",
});

const handleNameChange = () => {
  if (!nameChangeForm.newName || !nameChangeForm.reason) {
    message.error("请填写完整信息");
    return;
  }
  submitNameChange(nameChangeForm.newName, nameChangeForm.reason).then(() => {
    message.success("申请已提交，请等待审批");
    nameChangeVisible.value = false;
    nameChangeForm.newName = "";
    nameChangeForm.reason = "";
  });
};

const handleAvatarClick = () => {
  avatarModalVisible.value = true;
};

const beforeUpload = (file: File) => {
  const isJpgOrPng = file.type === "image/jpeg" || file.type === "image/png";
  if (!isJpgOrPng) {
    message.error("只能上传 JPG/PNG 格式的图片!");
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    message.error("图片大小不能超过 2MB!");
  }
  return isJpgOrPng && isLt2M;
};

const handleUploadAvatar = async (options: any) => {
  const { file, onSuccess, onError } = options;
  const formData = new FormData();
  formData.append("file", file);

  try {
    const res = await uploadFile(formData);
    const url = res.data.url;
    // Update profile with new avatar URL
    await updateProfile({ ...profileForm, avatarUrl: url });

    message.success("头像修改成功");
    userInfo.value.avatarUrl = url;
    profileForm.avatarUrl = url;
    onSuccess(res.data);
    avatarModalVisible.value = false;

    // Refresh user info
    fetchUserInfo();
  } catch (err: any) {
    message.error(err.message || "头像上传失败");
    onError(err);
  }
};

const pwdLoading = ref(false);
const pwdFormRef = ref();
const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: "",
});

// 密码校验规则
const validateNewPwd = async (_rule: any, value: string) => {
  if (!value) return Promise.reject("请输入新密码");
  const regex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
  if (!regex.test(value)) {
    return Promise.reject("密码需包含大小写字母、数字、特殊字符，至少8位");
  }
  if (value === pwdForm.oldPassword) {
    return Promise.reject("新密码不能与原密码相同");
  }
  return Promise.resolve();
};

const validateConfirmPwd = async (_rule: any, value: string) => {
  if (!value) return Promise.reject("请再次输入密码");
  if (value !== pwdForm.newPassword) {
    return Promise.reject("两次输入的密码不一致");
  }
  return Promise.resolve();
};

const pwdRules = {
  oldPassword: [{ required: true, message: "请输入原密码", trigger: "blur" }],
  newPassword: [{ validator: validateNewPwd, trigger: "blur" }],
  confirmPassword: [{ validator: validateConfirmPwd, trigger: "blur" }],
};

const fetchUserInfo = async () => {
  try {
    const res = await getUserInfo();
    const data = res.data;
    userInfo.value = {
      ...data.user,
      gradeName: data.gradeName,
      className: data.className,
    };
    userRole.value = data.role ? data.role.roleName : "普通用户";

    // Fill profile form
    profileForm.realName = data.user.realName;
    profileForm.phone = data.user.phone;
    profileForm.email = data.user.email;
    profileForm.avatarUrl = data.user.avatarUrl;
  } catch (error) {
    message.error("获取用户信息失败");
  }
};

const handleUpdateProfile = async () => {
  profileLoading.value = true;
  try {
    await updateProfile(profileForm);
    message.success("个人信息修改成功");
    await fetchUserInfo();
    // Update store name if needed
    userStore.name = profileForm.realName;
  } catch (error: any) {
    message.error(error.message || "修改失败");
  } finally {
    profileLoading.value = false;
  }
};

const handleUpdatePwd = () => {
  pwdFormRef.value
    .validate()
    .then(async () => {
      pwdLoading.value = true;
      try {
        await updatePassword(pwdForm);
        message.success("密码修改成功，请重新登录");

        // 强制退出并跳转登录
        Modal.success({
          title: "修改成功",
          content: "密码已修改，请使用新密码重新登录",
          onOk: async () => {
            // 1. 设置标记，防止 request.ts 重复处理 401
            (window as any).isReloginAlerted = true;
            // 2. 登出
            try {
              await userStore.logout();
            } catch (e) {
              // Ignore logout error (e.g. 401)
            }
            // 3. 跳转
            router.push("/login");
            // 4. 显示消息
            sessionStorage.setItem("loginMessage", "请使用新密码重新登录");
          },
        });
      } catch (error: any) {
        message.error(error.message || "密码修改失败");
      } finally {
        pwdLoading.value = false;
      }
    })
    .catch(() => {
      // Validation failed
    });
};

const formatDate = (date: string) => {
  return date ? dayjs(date).format("YYYY-MM-DD HH:mm:ss") : "-";
};

onMounted(() => {
  fetchUserInfo();
});
</script>

<style scoped>
.profile-container {
  padding: 20px;
}
.user-info-card {
  text-align: center;
  margin-bottom: 24px;
}
.user-profile-header {
  margin-bottom: 20px;
}
.avatar-wrapper {
  position: relative;
  display: inline-block;
  cursor: pointer;
  margin-bottom: 10px;
}
.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}
.avatar-modal-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px 0;
}
.avatar-preview {
  margin-bottom: 20px;
  border-radius: 50%;
  overflow: hidden;
  border: 1px solid #f0f0f0;
}
.avatar-preview :deep(.ant-image-img) {
  border-radius: 50%;
}
.upload-tip {
  margin-top: 10px;
  color: #999;
  font-size: 12px;
}
.user-name {
  font-size: 20px;
  font-weight: 500;
  margin-top: 16px;
  color: rgba(0, 0, 0, 0.85);
}
.user-role {
  margin-top: 4px;
  color: rgba(0, 0, 0, 0.45);
}
.user-profile-detail {
  text-align: left;
  padding: 0 20px;
}
.user-profile-detail p {
  margin-bottom: 12px;
  color: rgba(0, 0, 0, 0.65);
}
.user-profile-detail .anticon {
  margin-right: 8px;
  color: #1890ff;
}
</style>
