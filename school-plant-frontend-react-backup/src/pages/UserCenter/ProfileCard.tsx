import React, { useState, useEffect } from 'react';
import { Card, Skeleton, Avatar, Alert, Tag, Row, Col } from 'antd';
import { UserOutlined, PhoneOutlined, SafetyCertificateOutlined, CalendarOutlined, IdcardOutlined, NumberOutlined } from '@ant-design/icons';
import { getUserProfile, UserProfile } from '@/api/user';

const UserProfileCard: React.FC = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [userInfo, setUserInfo] = useState<UserProfile | null>(null);

  const fetchProfile = async () => {
    setLoading(true);
    setError(null);
    try {
      const res: any = await getUserProfile();
      setUserInfo(res);
    } catch (err) {
      setError('获取信息失败，请刷新重试');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  if (error) {
    return <Alert message={error} type="error" showIcon action={<a onClick={fetchProfile}>重试</a>} />;
  }

  // Statistic Label based on role
  const getStatLabel = (role: string) => {
    if (role === '管理员') return '负责审核申请数';
    if (role === '养护员') return '处理异常数';
    return '认养植物数量';
  };

  return (
    <Card className="shadow-md rounded-lg overflow-hidden border-0">
      <Skeleton loading={loading} avatar active paragraph={{ rows: 4 }}>
        {userInfo && (
          <div className="flex flex-col items-center">
            {/* Avatar Container */}
            <div className="mb-6 relative">
              <div className="w-24 h-24 rounded-full bg-green-100 flex items-center justify-center border-4 border-white shadow-sm overflow-hidden">
                {userInfo.avatarUrl ? (
                  <img src={userInfo.avatarUrl} alt="Avatar" className="w-full h-full object-cover" />
                ) : (
                  <span className="text-4xl">🌿</span> 
                )}
              </div>
              <Tag color={userInfo.role === '管理员' ? 'red' : userInfo.role === '养护员' ? 'blue' : 'green'} className="absolute -bottom-2 left-1/2 transform -translate-x-1/2 shadow-sm">
                {userInfo.role}
              </Tag>
            </div>

            {/* Info List */}
            <div className="w-full max-w-md bg-gray-50 rounded-lg p-6 space-y-4">
              <InfoItem icon={<UserOutlined />} label="账号" value={userInfo.account} />
              <InfoItem icon={<IdcardOutlined />} label="姓名" value={userInfo.name} />
              <InfoItem 
                icon={<NumberOutlined />} 
                label={userInfo.role === '普通用户' ? '学号/工号' : '工号'} 
                value={userInfo.idNumber} 
              />
              <InfoItem icon={<PhoneOutlined />} label="联系方式" value={userInfo.phone} />
              <InfoItem icon={<CalendarOutlined />} label="注册时间" value={userInfo.registerTime} />
              
              <div className="pt-4 mt-4 border-t border-gray-200">
                <div className="flex justify-between items-center">
                  <span className="text-gray-500 flex items-center gap-2">
                    <SafetyCertificateOutlined /> {getStatLabel(userInfo.role)}
                  </span>
                  <span className="text-xl font-bold text-green-600">{userInfo.statisticNum}</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </Skeleton>
    </Card>
  );
};

const InfoItem = ({ icon, label, value }: { icon: React.ReactNode, label: string, value: string }) => (
  <div className="flex justify-between items-center text-base">
    <span className="text-gray-500 flex items-center gap-2">
      {icon} {label}
    </span>
    <span className="font-medium text-gray-800">{value}</span>
  </div>
);

export default UserProfileCard;
