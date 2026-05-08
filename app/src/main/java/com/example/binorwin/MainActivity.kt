package com.example.binorwin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.binorwin.model.Argument
import com.example.binorwin.model.Post
import com.example.binorwin.network.RetrofitClient
import com.example.binorwin.ui.components.LoginScreen
import com.example.binorwin.ui.components.SignupScreen
import com.example.binorwin.ui.theme.BinOrWinTheme
import com.example.binorwin.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    // ViewModel for the Feed
    private val viewModel: MainViewModel by viewModels()
    // AuthViewModel - shared across auth screens
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize RetrofitClient with the application context
        RetrofitClient.init(this)

        // determine where the user should start
        // if they have a token, go to "feed", otherwise go to "login"
        val startDestination = if (RetrofitClient.isLoggedIn()) "feed" else "login"

        setContent {
            BinOrWinTheme {
                // create the Navigation Controller
                val navController = rememberNavController()

                // Set up the routes
                NavHost(navController = navController, startDestination = startDestination) {

                    // login route
                    composable("login") {
                        LoginScreen(
                            viewModel = authViewModel,
                            onNavigateToSignup = { navController.navigate("signup") },
                            onLoginSuccess = {
                                // If login succeeds, go to feed and remove login from backstack
                                navController.navigate("feed") {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // signup route
                    composable("signup") {
                        SignupScreen(
                            viewModel = authViewModel,
                            onNavigateToLogin = { navController.navigate("login") },
                            onSignupSuccess = {
                                // If signup succeeds, go to feed
                                navController.navigate("feed") {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        )
                    }

                    // feed route
                    composable("feed") {
                        MainScreen(
                            viewModel = viewModel,
                            onLogout = {
                                // delete token and username
                                RetrofitClient.clearAuthToken()
                                navController.navigate("login") {
                                    popUpTo("feed") { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel, onLogout: () -> Unit) {
    // State to control Bottom Sheet visibility and which post is currently selected
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<Int?>(null) }

    // Controls the animation and state of the bottom sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bin Or Win") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // logout button (top right)
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(modifier = Modifier.padding(paddingValues)) {
            if (viewModel.posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No posts yet. Pull to refresh or check server connection.")
                }
            } else {
                PostList(
                    posts = viewModel.posts,
                    onVote = { postId, type -> viewModel.vote(postId, type) },
                    onDiscussClick = { postId ->
                        selectedPostId = postId
                        viewModel.fetchArguments(postId)
                        showBottomSheet = true
                    }
                )
            }
        }
    }

    // display the Bottom Sheet if the state is true
    if (showBottomSheet && selectedPostId != null) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            modifier = Modifier.fillMaxHeight(0.85f) // Take up 85% of the screen height
        ) {
            ArgumentBottomSheetContent(
                postId = selectedPostId!!,
                arguments = viewModel.currentArguments,
                onSubmit = { actionType, content ->
                    viewModel.createArgument(selectedPostId!!, actionType, content)
                },
                // delete and edit functions to the bottom sheet
                onDelete = { argId ->
                    viewModel.deleteArgument(selectedPostId!!, argId)
                },
                onEdit = { argId, actionType, content ->
                    viewModel.updateArgument(selectedPostId!!, argId, actionType, content)
                },
                onLike = { argId ->
                    viewModel.likeArgument(selectedPostId!!, argId)
                }
            )
        }
    }
}

@Composable
fun PostList(posts: List<Post>, onVote: (Int, String) -> Unit, onDiscussClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts) { post ->
            PostCard(post = post, onVote = onVote, onDiscussClick = { onDiscussClick(post.id) })
        }
    }
}

@Composable
fun PostCard(post: Post, onVote: (Int, String) -> Unit, onDiscussClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, style = MaterialTheme.typography.headlineSmall)

            // the owner of the post
            val ownerName = post.owner?.username ?: "Unknown User"
            Text(
                text = "Posted by: $ownerName",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = post.imageUrl,
                contentDescription = "Post Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { onVote(post.id, "bin") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Bin (${post.binVotes})")
                }

                Button(
                    onClick = { onVote(post.id, "win") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Win (${post.winVotes})")
                }
            }

            // Discuss Button to open the bottom sheet
            TextButton(
                onClick = onDiscussClick,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
            ) {
                Text("View Arguments & Discuss (${post.argumentCount})")
            }
        }
    }
}

@Composable
fun ArgumentBottomSheetContent(
    postId: Int,
    arguments: List<Argument>,
    onSubmit: (String, String) -> Unit,
    onDelete: (Int) -> Unit, // ADDED THIS
    onEdit: (Int, String, String) -> Unit, // ADDED THIS
    onLike: (Int) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    var selectedActionType by remember { mutableStateOf("win") }

    // State variables for our Dialogs!
    var argumentToDelete by remember { mutableStateOf<Argument?>(null) }
    var argumentToEdit by remember { mutableStateOf<Argument?>(null) }
    var editContentText by remember { mutableStateOf("") }
    var editActionType by remember { mutableStateOf("win") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Arguments",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (arguments.isEmpty()) {
                item {
                    Text("No arguments yet. Be the first to discuss!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(arguments) { arg ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (arg.actionType == "win")
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                val argOwner = arg.owner?.username ?: "Anonymous"
                                Text(
                                    text = if (arg.actionType == "win") "WIN Argument by $argOwner" else "BIN Argument by $argOwner",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (arg.actionType == "win")
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = arg.content)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onLike(arg.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = "Like",
                                            tint = Color.Red
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${arg.likesCount}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }

                            // show icons (edit, delete) if it is current user content
                            if (arg.owner?.username == RetrofitClient.getUserName()) {
                                Row {
                                    IconButton(
                                        onClick = {
                                            // edit dialog open
                                            argumentToEdit = arg
                                            editContentText = arg.content
                                            editActionType = arg.actionType
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            // delete dialog open
                                            argumentToDelete = arg
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // delete confirmation
        if (argumentToDelete != null) {
            AlertDialog(
                onDismissRequest = { argumentToDelete = null },
                title = { Text("Delete Argument") },
                text = { Text("Are you sure you want to delete this argument? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        onDelete(argumentToDelete!!.id)
                        argumentToDelete = null
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { argumentToDelete = null }) { Text("Cancel") }
                }
            )
        }

        // edit
        if (argumentToEdit != null) {
            AlertDialog(
                onDismissRequest = { argumentToEdit = null },
                title = { Text("Edit Argument") },
                text = {
                    Column {
                        TextButton(
                            onClick = { editActionType = if (editActionType == "win") "bin" else "win" },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (editActionType == "win") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(editActionType.uppercase())
                        }
                        OutlinedTextField(
                            value = editContentText,
                            onValueChange = { editContentText = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (editContentText.isNotBlank()) {
                            onEdit(argumentToEdit!!.id, editActionType, editContentText)
                            argumentToEdit = null
                        }
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { argumentToEdit = null }) { Text("Cancel") }
                }
            )
        }

        // input area for new argument
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = { selectedActionType = if (selectedActionType == "win") "bin" else "win" },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (selectedActionType == "win") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            ) {
                Text(selectedActionType.uppercase())
            }

            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                placeholder = { Text("Write your argument...") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )

            IconButton(
                onClick = {
                    if (contentText.isNotBlank()) {
                        onSubmit(selectedActionType, contentText)
                        contentText = ""
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send Argument")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}