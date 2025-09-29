<template>
  <div class="banner">
    <div class="banner-inner">
      <div class="banner-copy">
        <div class="banner-title">{{ hero.slogan }}</div>
        <div class="banner-sub-title">{{ hero.subSlogan }}</div>
        <ul class="banner-highlights">
          <li v-for="item in highlights" :key="item.slogan">
            <span class="highlight-title">{{ item.slogan }}</span>
            <span class="highlight-text">{{ item.subSlogan }}</span>
          </li>
        </ul>
      </div>
      <img class="banner-image" :src="hero.image" alt="Login illustration" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import bannerImage from '@/assets/images/login-banner.png'

const { t } = useI18n()
const items = computed(() => [
  {
    slogan: t('login.banner.slogan1'),
    subSlogan: t('login.banner.subSlogan1'),
    image: bannerImage,
  },
  {
    slogan: t('login.banner.slogan2'),
    subSlogan: t('login.banner.subSlogan2'),
    image: bannerImage,
  },
  {
    slogan: t('login.banner.slogan3'),
    subSlogan: t('login.banner.subSlogan3'),
    image: bannerImage,
  },
])

const hero = computed(() => items.value[0])
const highlights = computed(() => items.value.slice(1))
</script>

<style lang="less" scoped>
.banner {
  display: flex;
  align-items: center;
  justify-content: center;

  &-inner {
    flex: 1;
    height: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    text-align: center;
  }
}
.banner-copy {
  max-width: 320px;
  margin-bottom: 24px;
}

.banner-title {
  color: #f8fafc;
  font-weight: 600;
  font-size: 22px;
  line-height: 30px;
}

.banner-sub-title {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.75);
  font-size: 14px;
  line-height: 22px;
}

.banner-highlights {
  list-style: none;
  margin: 20px 0 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.banner-highlights li {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 12px;
  padding: 10px 14px;
  text-align: left;
}

.highlight-title {
  display: block;
  font-weight: 600;
  color: #f8fafc;
}

.highlight-text {
  display: block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.65);
  margin-top: 4px;
}

.banner-image {
  width: 320px;
  max-width: 90%;
}

@media (min-width: 900px) {
  .banner-inner {
    align-items: flex-start;
    text-align: left;
  }

  .banner-copy {
    margin-bottom: 32px;
  }

  .banner-highlights {
    width: 100%;
  }
}
</style>
