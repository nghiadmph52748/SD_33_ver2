<template>
  <section class="not-found">
    <h1>{{ $t('page.notFound') }}</h1>
    <p>{{ $t('page.notFoundDesc') }}</p>
    <RouterLink to="/">
      <button class="return-home-btn">{{ $t('page.returnHome') }}</button>
    </RouterLink>
  </section>
</template>

<style scoped lang="scss">
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 24px;
  padding: 80px 20px;
  min-height: 60vh;
  justify-content: center;
}

.not-found h1 {
  font-size: clamp(32px, 6vw, 64px);
  font-weight: 700;
  color: #111;
  margin: 0;
  line-height: 1.2;
}

.not-found p {
  font-size: clamp(16px, 2vw, 18px);
  color: #6b7280;
  margin: 0;
  max-width: 600px;
  line-height: 1.6;
}

.return-home-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 24px;
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  background: #111;
  border: 1px solid #111;
  border-radius: 8px;
  cursor: pointer;
  text-decoration: none;
  transition: background-color 0.2s ease, transform 0.1s ease, box-shadow 0.2s ease;
  margin-top: 8px;
}

.return-home-btn:hover {
  background: #333;
  border-color: #333;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.return-home-btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.return-home-btn:focus-visible {
  outline: none;
  box-shadow: 0 0 0 3px rgba(17, 17, 17, 0.2);
}
</style>
