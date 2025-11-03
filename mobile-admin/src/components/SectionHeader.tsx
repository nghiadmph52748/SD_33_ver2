import React from 'react'
import { StyleSheet, View } from 'react-native'
import { Text } from 'react-native-paper'

interface SectionHeaderProps {
  title: string
  action?: React.ReactNode
}

const SectionHeader: React.FC<SectionHeaderProps> = ({ title, action }) => (
  <View style={styles.container}>
    <Text variant="titleMedium" style={styles.title}>
      {title}
    </Text>
    {action}
  </View>
)

const styles = StyleSheet.create({
  container: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  title: {
    fontWeight: '600',
  },
})

export default SectionHeader
