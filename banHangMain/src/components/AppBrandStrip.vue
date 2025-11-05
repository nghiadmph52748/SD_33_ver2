<template>
<section class="brand-strip" :aria-label="$t('aria.brandNav')">
    <div class="container narrow">
      <ul class="brand-list">
      <li>
        <RouterLink to="/products?brand=nike" class="brand-link" aria-label="Nike">
          <svg viewBox="0 0 64 22" width="64" height="22" aria-hidden="true">
            <path fill="#111111" d="M17.7277 12.1511C15.999 12.598 14.4241 12.8196 13.0469 12.8196C11.3396 12.8196 9.94617 12.4728 8.97074 11.7793C4.02962 8.28845 8.54956 0.885548 9.06118 0.0629324C6.88551 2.37923 4.65235 4.80341 2.89851 7.44593C-0.0575023 11.9597 -0.812655 16.2475 0.910825 18.906C2.23896 20.9642 4.40042 22 7.37517 22C10.0146 22 13.2975 21.1832 17.1928 19.5559L64 0.0173385L63.9981 0L17.7277 12.1511Z" />
          </svg>
          <span>Nike</span>
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/products?brand=jordan" class="brand-link" aria-label="Jordan">
          <svg viewBox="0 0 24 24" width="24" height="24" aria-hidden="true">
            <path fill="#111111" d="M13.182 3.733a.97.97 0 111.917-.157.964.964 0 01-.917 1.007c-.027.004-.062 0-.101 0-.16.35-.216.97-.246 1.327-.09.534-.14.8-.328 1.29-.017.4.01.46-.281 1.101-.28.85.018 1.228.348 1.939.686.314 1.61.996 2.483 1.808.26.153.933.687 1.42 1.248.402.46.513.923.382 1.278-.18.49-.77.603-1.49.687-.59.067-1.433.12-2.257.12-.642 0-1.263-.03-1.735-.093-1.04-.136-1.89-.49-2.52-1.046-.39-.339-.71-.77-.93-1.216-.086-.176-.162-.355-.226-.53-.21-.566-.26-1.03-.277-1.278-.204-.084-.702-.4-.908-.755-.223-.384.07-.69.263-.851.23-.19.553-.312.905-.384.3-.06.63-.084.92-.084.243 0 .464.015.637.04.042-.313.105-.745.207-1.265.093-.484.207-.946.345-1.372.162-.504.33-.878.492-1.15z" />
          </svg>
          <span>Jordan</span>
        </RouterLink>
      </li>
      <li>
        <RouterLink to="/products?brand=adidas" class="brand-link" aria-label="Adidas">
          <svg viewBox="0 0 64 24" width="64" height="24" aria-hidden="true">
            <path fill="#111111" d="M6 18l10-12 4 6 10-12 8 12 8-12 6 18H6z" />
          </svg>
          <span>Adidas</span>
        </RouterLink>
      </li>
      </ul>
    </div>
  </section>
</template>

<style scoped lang="scss">
.brand-strip {
  display: flex;
  justify-content: center;
  padding: 10px 0;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
  background: #ffffff;
  margin-top: 0;
  position: relative;
  z-index: 5; /* ensure hero fade does not cover brand nav */
}

.brand-list {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  align-items: center;
  justify-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
}

.brand-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #111111;
  text-decoration: none;
  padding: 6px 8px;
  border-radius: 8px;
  transition: background 0.2s ease;
}

.brand-link span {
  font-size: 14px;
  color: rgba(17, 17, 17, 0.8);
}

.brand-link svg { height: 28px; width: auto; }

.brand-link:hover {
  background: #f5f5f5;
}

@media (max-width: 600px) {
  .brand-list {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .brand-link {
    justify-content: center;
  }
}
</style>

