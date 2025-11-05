<template>
  <section class="boxes" :aria-label="$t('home.sections.sales')">
    <div v-for="(box, index) in boxes" :key="box.heading" :class="`box${index}`">
      <img :src="box.icon" :alt="box.alt" />
      <h4>{{ box.heading }}</h4>
      <p>{{ box.details }}</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
interface SalesBox {
  icon: string;
  alt: string;
  heading: string;
  details: string;
}

const { t } = useI18n();
const boxes: SalesBox[] = [
  {
    icon: "/icon-package.svg",
    alt: t('sales.shippingAlt'),
    heading: t('sales.shippingHeading'),
    details: t('sales.shippingDesc')
  },
  {
    icon: "/icon-cal.svg",
    alt: t('sales.supportAlt'),
    heading: t('sales.supportHeading'),
    details: t('sales.supportDesc')
  },
  {
    icon: "/icon-service.svg",
    alt: t('sales.returnsAlt'),
    heading: t('sales.returnsHeading'),
    details: t('sales.returnsDesc')
  }
];
</script>

<style scoped lang="scss">
.boxes {
  margin: 70px 0;

  div {
    border: 1px solid #ddd;
    min-height: 200px; /* ensure enough space for longer VI text */
    text-align: center;
    display: flex;
    flex-direction: column;
    justify-content: center; /* vertically center content */
    align-items: center;
    height: 100%;
  }

  .box1 {
    grid-area: 1 / 1 / 2 / 2;
  }

  .box2 {
    grid-area: 1 / 2 / 2 / 3;
  }

  .box3 {
    grid-area: 1 / 3 / 2 / 4;
  }
}

h4 {
  text-transform: uppercase;
  letter-spacing: 0.15em;
  margin: 0 0 10px 0;
}

@media (min-width: 700px) {
  .boxes {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    grid-template-rows: 1fr;
    grid-column-gap: 20px;
    align-items: stretch; /* make all boxes equal height in the row */

    div {
      padding: 40px 50px;
    }
  }
}

@media (max-width: 699px) {
  .boxes {
    width: 83vw;
    margin-left: 5vw;

    div {
      padding: 20px;
      margin-bottom: 10px;
    }
  }
}
</style>
