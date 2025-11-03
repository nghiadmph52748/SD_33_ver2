import React from 'react'
import { StyleSheet } from 'react-native'
import { Searchbar } from 'react-native-paper'

interface SearchInputProps {
  query: string
  placeholder?: string
  onChangeQuery: (value: string) => void
  onSubmit?: () => void
}

const SearchInput: React.FC<SearchInputProps> = ({ query, placeholder, onChangeQuery, onSubmit }) => (
  <Searchbar
    value={query}
    placeholder={placeholder ?? 'Tìm kiếm'}
    onChangeText={onChangeQuery}
    onSubmitEditing={onSubmit}
    style={styles.searchBar}
  />
)

const styles = StyleSheet.create({
  searchBar: {
    marginHorizontal: 16,
    marginBottom: 12,
    borderRadius: 12,
  },
})

export default SearchInput
