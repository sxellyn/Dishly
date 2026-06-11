package com.dishly.app.data

import com.dishly.app.R
import com.dishly.app.model.Comment
import com.dishly.app.model.Recipe
import com.dishly.app.model.User

/**
 * Data layer (MVVM) — in-memory repository, no backend.
 * ViewModels consume this class; the UI never accesses data directly.
 */
object RecipeRepository {

    val currentUser = User(
        name = "Suellyn Gomes",
        username = "Suellyn_Gomes"
    )

    val recipes: MutableList<Recipe> = mutableListOf(
        Recipe(
            id = 1,
            title = "Chocolate cake with buttercream frosting",
            rating = 344,
            durationMin = 45,
            difficulty = "Medium",
            imageRes = R.drawable.foodie,
            description = "A rich and moist chocolate cake topped with a smooth buttercream frosting. Perfect for celebrations or a sweet weekend treat.",
            ingredients = listOf(
                "2 cups flour", "1 cup sugar", "3/4 cup cocoa powder",
                "2 eggs", "1 cup milk", "200g butter", "1 tsp baking powder"
            ),
            steps = listOf(
                "In a large bowl, mix together flour, baking powder, sugar and cocoa.",
                "Add the eggs, milk and melted butter and mix until smooth.",
                "Pour the batter into a greased pan and bake at 180°C for 35 minutes.",
                "Let it cool, then spread the buttercream frosting on top."
            ),
            tags = listOf("Cake", "Dessert", "Chocolate"),
            isPopular = true,
            isLatest = true,
            isFavorite = true,
            comments = mutableListOf(
                Comment(1, "Kelly Mayer", "Turned out amazing, the frosting is perfect!", "2 days ago"),
                Comment(2, "John Doe", "Added a bit more cocoa and loved it.", "1 day ago")
            )
        ),
        Recipe(
            id = 2,
            title = "Special icecream",
            rating = 211,
            durationMin = 20,
            difficulty = "Easy",
            imageRes = R.drawable.foodie,
            description = "A creamy homemade icecream that you can customize with your favorite toppings.",
            ingredients = listOf("2 cups cream", "1 can condensed milk", "1 tsp vanilla", "Toppings"),
            steps = listOf(
                "Whip the cream until soft peaks form.",
                "Fold in the condensed milk and vanilla.",
                "Freeze for at least 6 hours.",
                "Serve with your favorite toppings."
            ),
            tags = listOf("Dessert", "Cold"),
            isPopular = true
        ),
        Recipe(
            id = 3,
            title = "Strawberry cake with blueberry cream",
            rating = 148,
            durationMin = 50,
            difficulty = "Medium",
            imageRes = R.drawable.foodie,
            description = "A fruity sponge cake layered with fresh strawberries and a light blueberry cream.",
            ingredients = listOf("Sponge cake base", "Fresh strawberries", "Blueberries", "Whipped cream", "Sugar"),
            steps = listOf(
                "Prepare the sponge cake and let it cool.",
                "Whip the cream with the blueberries.",
                "Layer the cake with strawberries and cream.",
                "Chill before serving."
            ),
            tags = listOf("Cake", "Fruit"),
            isPopular = true,
            isLatest = true
        ),
        Recipe(
            id = 4,
            title = "Frosted pinecone cake",
            rating = 132,
            durationMin = 60,
            difficulty = "Hard",
            imageRes = R.drawable.foodie,
            description = "An impressive decorated cake shaped and frosted to look like a pinecone.",
            ingredients = listOf("Vanilla cake", "Chocolate frosting", "Almond flakes", "Sugar"),
            steps = listOf(
                "Bake the vanilla cake and shape it.",
                "Cover with chocolate frosting.",
                "Decorate with almond flakes to imitate a pinecone."
            ),
            tags = listOf("Cake", "Dessert"),
            isLatest = true,
            isFavorite = true
        ),
        Recipe(
            id = 5,
            title = "Classic Victoria sandwich recipe",
            rating = 98,
            durationMin = 40,
            difficulty = "Easy",
            imageRes = R.drawable.foodie,
            description = "A traditional British sponge cake filled with jam and cream.",
            ingredients = listOf("Butter", "Sugar", "Eggs", "Self-raising flour", "Strawberry jam"),
            steps = listOf(
                "Cream the butter and sugar together.",
                "Beat in the eggs and fold in the flour.",
                "Bake in two tins and sandwich with jam and cream."
            ),
            tags = listOf("Cake", "Classic"),
            isLatest = true
        ),
        Recipe(
            id = 6,
            title = "Perfect homemade pancake",
            rating = 421,
            durationMin = 25,
            difficulty = "Easy",
            imageRes = R.drawable.foodie,
            description = "Fluffy homemade pancakes that are quick to make with simple ingredients you already have.",
            ingredients = listOf(
                "1 1/2 cups flour", "3 1/2 tsp baking powder", "1 tsp salt",
                "1 tbsp sugar", "1 1/4 cups milk", "1 egg", "3 tbsp melted butter"
            ),
            steps = listOf(
                "In a large bowl, mix together flour, baking powder, sugar and salt.",
                "Make a well in the center and pour in the milk, egg and melted butter.",
                "Mix until smooth.",
                "Heat a lightly oiled pan and pour the batter.",
                "Brown on both sides and serve hot."
            ),
            tags = listOf("Breakfast", "Quick"),
            isPopular = true,
            isLatest = true,
            isFavorite = true,
            comments = mutableListOf(
                Comment(3, "John Doe", "Best pancakes I've ever made, so fluffy!", "3 days ago"),
                Comment(4, "Anna Smith", "My kids loved it. Will make again.", "yesterday")
            )
        )
    )

    /** Labels for ingredient chips on the Search screen (selection is UI-only until API is wired). */
    val pickerIngredients: List<String> = listOf(
        "Egg", "Tomato", "Lettuce", "Onion", "Garlic", "Flour", "Pepper", "Beef",
        "Banana", "Rice", "Olive oil", "Cream cheese", "Lemon", "Carrot",
        "Orange", "Yogurt", "Vinegar", "Mushroom", "Pasta", "Lentil", "Strawberry",
        "Watermelon", "Apple", "Grape", "Chocolate", "Milk", "Butter", "Sugar"
    )

    fun popularRecipes(): List<Recipe> = recipes.filter { it.isPopular }
    fun latestRecipes(): List<Recipe> = recipes.filter { it.isLatest }
    fun favoriteRecipes(): List<Recipe> = recipes.filter { it.isFavorite }
    fun recipeById(id: Int): Recipe? = recipes.find { it.id == id }

    fun toggleFavorite(recipeId: Int) {
        recipeById(recipeId)?.let { it.isFavorite = !it.isFavorite }
    }

    fun addComment(recipeId: Int, text: String): Comment? {
        val recipe = recipeById(recipeId) ?: return null
        val comment = Comment(
            id = (recipe.comments.maxOfOrNull { it.id } ?: 0) + 1,
            authorName = currentUser.name,
            text = text,
            timeLabel = "now"
        )
        recipe.comments.add(comment)
        return comment
    }

    fun updateUser(name: String, username: String) {
        currentUser.name = name
        currentUser.username = username
    }
}
