import React from 'react'
import { StyleSheet, View } from 'react-native'
import { Card, Text } from 'react-native-paper'

export interface MetricCardProps {
  title: string
  value: string
  subtitle?: string
  accentColor?: string
}

const MetricCard: React.FC<MetricCardProps> = ({ title, value, subtitle, accentColor }) => (
  <Card style={styles.card}>
    <View style={styles.container}>
      <View style={[styles.indicator, accentColor ? { backgroundColor: accentColor } : null]} />
      <View style={styles.content}>
        <Text variant="labelLarge" style={styles.title}>
          {title}
        </Text>
        <Text variant="headlineMedium" style={styles.value}>
          {value}
        </Text>
        {subtitle ? (
          <Text variant="bodySmall" style={styles.subtitle}>
            {subtitle}
          </Text>
        ) : null}
      </View>
    </View>
  </Card>
)

const styles = StyleSheet.create({
  card: {
    flex: 1,
    margin: 8,
    borderRadius: 16,
    elevation: 2,
  },
  container: {
    flexDirection: 'row',
    alignItems: 'stretch',
    paddingHorizontal: 16,
    paddingVertical: 20,
  },
  indicator: {
    width: 6,
    minHeight: 60,
    borderRadius: 16,
    backgroundColor: '#2563eb',
    marginRight: 16,
  },
  content: {
    flex: 1,
    justifyContent: 'center',
  },
  title: {
    color: '#475569',
    marginBottom: 4,
  },
  value: {
    fontWeight: '700',
  },
  subtitle: {
    color: '#64748b',
    marginTop: 4,
  },
})

export default MetricCard
