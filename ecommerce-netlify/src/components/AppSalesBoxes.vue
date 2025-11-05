<template>
  <section class="boxes">
    <div v-for="(box, index) in boxes" :key="box.heading" :class="`box${index}`">
      <img :src="box.icon" alt="service icon" />
      <h4>{{ box.heading }}</h4>
      <p>{{ box.details }}</p>
    </div>
  </section>
</template>

<script setup lang="ts">
interface SalesBox {
  icon: string;
  heading: string;
  details: string;
}

const boxes: SalesBox[] = [
  {
    icon: "/icon-package.svg",
    heading: "Free Shipping Globally",
    details: "Delivery in 4 Days"
  },
  {
    icon: "/icon-cal.svg",
    heading: "24/7 Customer Service",
    details: "Call us any time"
  },
  {
    icon: "/icon-service.svg",
    heading: "Easy Online Returns",
    details: "Send Within 30 Days"
  }
];
</script>

<style scoped lang="scss">
.boxes {
  margin: 70px 0;

  div {
    border: 1px solid #ddd;
    min-height: 150px;
    justify-self: center;
    align-self: center;
    text-align: center;
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
      padding: 10px 20px;
      margin-bottom: 10px;
    }
  }
}
</style>
