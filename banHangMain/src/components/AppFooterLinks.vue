<template>
  <section class="footer-links">
    <div class="footer-links__column">
      <h4>{{ $t('footerLinks.resources') }}</h4>
      <ul>
        <li>{{ $t('footerLinks.findStore') }}</li>
        <li>{{ $t('footerLinks.becomeMember') }}</li>
        <li>{{ $t('footerLinks.runningShoeFinder') }}</li>
        <li>{{ $t('footerLinks.nikeCoaching') }}</li>
        <li>{{ $t('footerLinks.sendFeedback') }}</li>
      </ul>
    </div>

    <div class="footer-links__column">
      <h4>{{ $t('footerLinks.help') }}</h4>
      <ul>
        <li>{{ $t('footerLinks.getHelp') }}</li>
        <li>{{ $t('footerLinks.orderStatus') }}</li>
        <li>{{ $t('footerLinks.delivery') }}</li>
        <li>{{ $t('footerLinks.returns') }}</li>
        <li>{{ $t('footerLinks.paymentOptions') }}</li>
        <li>{{ $t('footerLinks.contactUs') }}</li>
      </ul>
    </div>

    <div class="footer-links__column">
      <h4>{{ $t('footerLinks.company') }}</h4>
      <ul>
        <li>{{ $t('footerLinks.aboutNike') }}</li>
        <li>{{ $t('footerLinks.news') }}</li>
        <li>{{ $t('footerLinks.careers') }}</li>
        <li>{{ $t('footerLinks.investors') }}</li>
        <li>{{ $t('footerLinks.sustainability') }}</li>
        <li>{{ $t('footerLinks.impact') }}</li>
        <li>{{ $t('footerLinks.reportConcern') }}</li>
      </ul>
    </div>
  </section>
</template>

<script setup lang="ts"></script>

<style scoped lang="scss">
.footer-links {
  width: min(1180px, 100%);
  margin: 0 auto;
  padding: clamp(32px, 5vw, 56px) 0;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: clamp(24px, 4vw, 40px);
  color: #1d2129;
}

.footer-links__column h4 {
  margin: 0 0 12px;
  font-size: 16px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: #4e5969;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 10px;
  color: #4e5969;
}

@media (max-width: 900px) {
  .footer-links { grid-template-columns: 1fr; }
}
</style>
