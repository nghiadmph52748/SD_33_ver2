import React from 'react'
import { Pressable, PressableProps, StyleSheet } from 'react-native'
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withSpring,
  withTiming,
} from 'react-native-reanimated'

const AnimatedPressable = Animated.createAnimatedComponent(Pressable)

interface AnimatedPressableProps extends Omit<PressableProps, 'style'> {
  children: React.ReactNode
  style?: PressableProps['style']
  scaleTo?: number
  opacityTo?: number
}

/**
 * A pressable component with smooth scale and opacity animations
 * @param scaleTo - Scale value when pressed (default: 0.95)
 * @param opacityTo - Opacity value when pressed (default: 0.8)
 */
export const AnimatedPress: React.FC<AnimatedPressableProps> = ({
  children,
  style,
  scaleTo = 0.95,
  opacityTo = 0.8,
  onPressIn,
  onPressOut,
  ...props
}) => {
  const scale = useSharedValue(1)
  const opacity = useSharedValue(1)

  const animatedStyle = useAnimatedStyle(() => {
    return {
      transform: [{ scale: scale.value }],
      opacity: opacity.value,
    }
  })

  const handlePressIn = (e: any) => {
    scale.value = withSpring(scaleTo, {
      damping: 15,
      stiffness: 300,
    })
    opacity.value = withTiming(opacityTo, { duration: 100 })
    onPressIn?.(e)
  }

  const handlePressOut = (e: any) => {
    scale.value = withSpring(1, {
      damping: 15,
      stiffness: 300,
    })
    opacity.value = withTiming(1, { duration: 150 })
    onPressOut?.(e)
  }

  return (
    <AnimatedPressable
      style={[animatedStyle, style]}
      onPressIn={handlePressIn}
      onPressOut={handlePressOut}
      {...props}
    >
      {children}
    </AnimatedPressable>
  )
}

