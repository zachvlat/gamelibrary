import React, { useState } from 'react';
import { View, Text, FlatList, Image, ScrollView, TouchableOpacity } from 'react-native';
import { styles } from '../styles';

export default function AllSourcesScreen({ importedData }) {
    const [sortOption, setSortOption] = useState('communityScore');
    
    const sortedGames = [...importedData].sort((a, b) => {
        switch (sortOption) {
            case 'alphabetical':
                return a.Name.localeCompare(b.Name);
            case 'communityScore':
                return (b.CommunityScore || 0) - (a.CommunityScore || 0);
            case 'criticScore':
                return (b.CriticScore || 0) - (a.CriticScore || 0);
            case 'releaseDate':
                return new Date(b.ReleaseDate) - new Date(a.ReleaseDate);
            case 'added':
                return new Date(b.Added) - new Date(a.Added);
            case 'playtime':
                return (b.Playtime || 0) - (a.Playtime || 0);
            default:
                return 0;
        }
    });

    const sortOptions = [
        { id: 'alphabetical', label: 'Alphabetical' },
        { id: 'communityScore', label: 'Community Score' },
        { id: 'criticScore', label: 'Critic Score' },
        { id: 'releaseDate', label: 'Release Date' },
        { id: 'added', label: 'Added' },
        { id: 'playtime', label: 'Playtime' }
    ];

    return (
        <View style={styles.screenContainer}>
            <ScrollView 
                horizontal 
                showsHorizontalScrollIndicator={false}
                style={styles.chipContainer}
                contentContainerStyle={styles.chipContentContainer}
            >
                {sortOptions.map((option) => (
                    <TouchableOpacity
                        key={option.id}
                        style={[
                            styles.chip,
                            sortOption === option.id && styles.chipActive
                        ]}
                        onPress={() => setSortOption(option.id)}
                    >
                        <Text style={[
                            styles.chipText,
                            sortOption === option.id && styles.chipTextActive
                        ]}>
                            {option.label}
                        </Text>
                    </TouchableOpacity>
                ))}
            </ScrollView>
            
            <FlatList
                contentContainerStyle={{ paddingBottom: 80 }}
                data={sortedGames}
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
                            {sortOption === 'communityScore' && item.CommunityScore && (
                                <Text style={styles.text}>Community Score: {item.CommunityScore}</Text>
                            )}
                            {sortOption === 'criticScore' && item.CriticScore && (
                                <Text style={styles.text}>Critic Score: {item.CriticScore}</Text>
                            )}
                            {sortOption === 'playtime' && item.Playtime && (
                                <Text style={styles.text}>Playtime: {Math.floor(item.Playtime / 60)} hours</Text>
                            )}
                        </View>
                    </View>
                )}
            />
        </View>
    );
}