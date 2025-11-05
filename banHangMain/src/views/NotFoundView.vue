<template>
  <section class="not-found">
    <h1>{{ $t('page.notFound') }}</h1>
    <p>The page you're looking for doesn't exist. Head back to the storefront to continue shopping.</p>
    <RouterLink to="/">
      <button>Return Home</button>
    </RouterLink>
  </section>
</template>

<style scoped lang="scss">
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 16px;
  padding: 80px 0;
}
</style>
