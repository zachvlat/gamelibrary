import React, { useState, useMemo } from 'react';
import { View, Text, FlatList, TextInput, Image } from 'react-native';
import { styles } from '../styles';

export default function SearchScreen({ importedData }) {
  const [searchQuery, setSearchQuery] = useState('');
  
  const filteredGames = useMemo(() => {
    if (!searchQuery) return [];
    
    const query = searchQuery.toLowerCase();
    return importedData.filter(game => 
      game.Name.toLowerCase().includes(query) ||
      (game.Genres && game.Genres.some(genre => genre.toLowerCase().includes(query))) ||
      (game.Sources && game.Sources.toLowerCase().includes(query))
    );
  }, [searchQuery, importedData]);

  return (
    <View style={styles.screenContainer}>
      <TextInput
        style={styles.searchInput}
        placeholder="Search games by name/genre..."
        placeholderTextColor="#999"
        value={searchQuery}
        onChangeText={setSearchQuery}
        autoCapitalize="none"
        autoCorrect={false}
      />
      
      {searchQuery ? (
        <FlatList
          contentContainerStyle={{ paddingBottom: 80 }}
          data={filteredGames}
          keyExtractor={(item) => item.Id}
          renderItem={({ item }) => (
            <View style={styles.rowCard}>
              <Image source={{ uri: item.CoverArtUrl }} style={styles.thumbnail} />
              <View style={styles.cardContent}>
                <Text style={styles.gameTitle}>{item.Name}</Text>
                <Text style={styles.text}>
                  {item.Genres ? item.Genres.join(', ') : 'No genres available'}
                </Text>
                {item.Sources && (
                  <Text style={styles.sourceText}>{item.Sources}</Text>
                )}
              </View>
            </View>
          )}
          ListEmptyComponent={
            <Text style={styles.noResultsText}>No games found matching your search</Text>
          }
        />
      ) : (
        <View style={styles.centeredMessage}>
          <Text style={styles.text}>Enter a search term to find your games</Text>
        </View>
      )}
    </View>
  );
}