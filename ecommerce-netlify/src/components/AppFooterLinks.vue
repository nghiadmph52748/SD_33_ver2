<template>
  <section class="footer-links">
    <div class="footer-links__column footer-links__brand">
      <h3 class="footer-links__title">GearUp</h3>
      <p class="footer-links__tagline">
        Premium sneakers, curated collections, and member-only releases delivered across Vietnam.
      </p>
      <div class="footer-links__social">
        <a
          v-for="social in socialLinks"
          :key="social.name"
          :href="social.href"
          target="_blank"
          rel="noopener"
          :aria-label="social.name"
          class="footer-links__social-item"
        >
          <span class="footer-links__social-icon">
            <svg :viewBox="social.icon.viewBox" xmlns="http://www.w3.org/2000/svg" role="img">
              <path :d="social.icon.path" fill="currentColor" />
            </svg>
          </span>
          <span>{{ social.name }}</span>
        </a>
      </div>
    </div>

    <div class="footer-links__column">
      <h4>Explore</h4>
      <ul>
        <li v-for="link in exploreLinks" :key="link">{{ link }}</li>
      </ul>
    </div>

    <div class="footer-links__column">
      <h4>Support</h4>
      <ul>
        <li v-for="link in supportLinks" :key="link">{{ link }}</li>
      </ul>
    </div>

    <div class="footer-links__column footer-links__newsletter">
      <h4>Newsletter</h4>
      <p>Subscribe for early access to drops, rewards, and style guides.</p>
      <form class="footer-links__form" @submit.prevent>
        <label class="sr-only" for="footer-email">Email address</label>
        <input
          id="footer-email"
          type="email"
          name="email"
          autocomplete="email"
          placeholder="you@example.com"
          required
        />
        <button type="submit">Join now</button>
      </form>
    </div>
  </section>
</template>

<script setup lang="ts">
interface SocialLink {
  name: string;
  href: string;
  icon: {
    viewBox: string;
    path: string;
  };
}

const exploreLinks = ["About", "Collections", "Store locator", "Gift cards"];
const supportLinks = ["Help center", "Shipping & returns", "Track order", "Privacy policy"];

const socialLinks: SocialLink[] = [
  {
    name: "Instagram",
    href: "https://www.instagram.com/",
    icon: {
      viewBox: "0 0 24 24",
      path: "M7.5 2h9A5.5 5.5 0 0 1 22 7.5v9a5.5 5.5 0 0 1-5.5 5.5h-9A5.5 5.5 0 0 1 2 16.5v-9A5.5 5.5 0 0 1 7.5 2zm0 2C6.12 4 5 5.12 5 6.5v11C5 18.88 6.12 20 7.5 20h9c1.38 0 2.5-1.12 2.5-2.5v-11C19 5.12 17.88 4 16.5 4h-9zm4.5 3.5A4.5 4.5 0 1 1 7.5 12 4.5 4.5 0 0 1 12 7.5zm0 2A2.5 2.5 0 1 0 14.5 12 2.5 2.5 0 0 0 12 9.5zm5-3.75a1.25 1.25 0 1 1-1.25 1.25A1.25 1.25 0 0 1 17 5.75z"
    }
  },
  {
    name: "Facebook",
    href: "https://www.facebook.com/",
    icon: {
      viewBox: "0 0 24 24",
      path: "M22 12a10 10 0 1 0-11.6 9.87v-6.99H7.9V12h2.5V9.79c0-2.48 1.48-3.85 3.74-3.85 1.08 0 2.22.19 2.22.19v2.44h-1.25c-1.23 0-1.61.76-1.61 1.54V12h2.74l-.44 2.88h-2.3v6.99A10 10 0 0 0 22 12Z"
    }
  },
  {
    name: "X",
    href: "https://www.twitter.com/",
    icon: {
      viewBox: "0 0 24 24",
      path: "M3 3h4.61l4.38 6.18L17.5 3H21l-7.3 9.95L21.5 21H16.9l-4.82-6.79L6.9 21H3.5l7.8-10.51z"
    }
  },
  {
    name: "YouTube",
    href: "https://www.youtube.com/",
    icon: {
      viewBox: "0 0 24 24",
      path: "M21.6 7.2a2.5 2.5 0 0 0-1.76-1.77C18.22 5 12 5 12 5s-6.22 0-7.84.43A2.5 2.5 0 0 0 2.4 7.2 26.3 26.3 0 0 0 2 12a26.3 26.3 0 0 0 .4 4.8 2.5 2.5 0 0 0 1.76 1.77C5.78 19 12 19 12 19s6.22 0 7.84-.43a2.5 2.5 0 0 0 1.76-1.77A26.3 26.3 0 0 0 22 12a26.3 26.3 0 0 0-.4-4.8ZM10 15.5v-7l6 3.5Z"
    }
  }
];
</script>

<style scoped lang="scss">
.footer-links {
  width: min(1180px, 100%);
  margin: 0 auto;
  padding: clamp(32px, 5vw, 56px) 0;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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

.footer-links__brand {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.footer-links__title {
  font-size: clamp(22px, 4vw, 28px);
  font-weight: 700;
  margin: 0;
}

.footer-links__tagline {
  margin: 0;
  color: #4e5969;
  line-height: 1.5;
}

.footer-links__social {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.footer-links__social-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid rgba(22, 119, 255, 0.15);
  color: #1677ff;
  background: rgba(22, 119, 255, 0.08);
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s ease;
}

.footer-links__social-item:hover {
  background: #1677ff;
  color: #ffffff;
  box-shadow: 0 12px 24px rgba(22, 119, 255, 0.22);
}

.footer-links__social-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
}

.footer-links__social-icon svg {
  width: 18px;
  height: 18px;
}

ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: grid;
  gap: 10px;
  color: #4e5969;
}

.footer-links__newsletter p {
  margin: 0 0 12px;
  color: #4e5969;
  line-height: 1.5;
}

.footer-links__form {
  display: grid;
  gap: 12px;
  align-items: center;
  grid-template-columns: 1fr auto;
  background: rgba(22, 119, 255, 0.08);
  padding: 12px;
  border-radius: 16px;
}

.footer-links__form input {
  border: none;
  background: transparent;
  padding: 10px 12px;
  border-radius: 12px;
  font-size: 14px;
  color: #1d2129;
}

.footer-links__form input:focus {
  outline: none;
}

.footer-links__form button {
  border: none;
  background: #1677ff;
  color: #ffffff;
  padding: 10px 18px;
  border-radius: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.footer-links__form button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(22, 119, 255, 0.2);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
}

@media (max-width: 640px) {
  .footer-links__form {
    grid-template-columns: 1fr;
  }

  .footer-links__social {
    gap: 8px;
  }
}
</style>
